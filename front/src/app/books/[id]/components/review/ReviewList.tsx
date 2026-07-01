import { toggleReviewLike } from "@/api/reviews";
import { formatDate } from "@/app/utils/formatData";
import StarRating from "@/components/common/StarRating";
import { useDisclosure } from "@/hooks/common/useDisclosure";
import { Review } from "@/types/reviews";
import { Dispatch, SetStateAction, useEffect, useState } from "react";
import ActionDropdown from "./ActionDropdown";
import ReviewDeleteModal from "./ReviewDeleteModal";
import { useAuthStore } from "@/store/authStore";
import { isAdmin } from "@/utils/authRole";
import DelayedLoader from "@/components/common/DelayedLoader";
import InfiniteScrollLoader from "@/components/common/InfiniteScrollLoader";
import EditContainer from "./EditContainer";
import ReviewRating from "./ReviewRating";
import Label from "@/components/common/Buttons/Label";
import getImagePath from "@/constants/images.ts";

export default function ReviewList({
  data,
  setData,
  isLoading,
  setTotalElements,
  bookId
}: {
  data: Review[];
  setData: Dispatch<SetStateAction<Review[]>>;
  isLoading: boolean;
  setTotalElements: Dispatch<SetStateAction<number>>;
  bookId: string;
}) {
  const [reviews, setReviews] = useState<Review[]>(data);
  const [reviewId, setReviewId] = useState("");
  const [editingReviewId, setEditingReviewId] = useState<string | null>(null);
  const [rating, setRating] = useState(1);

  const { user } = useAuthStore();
  const { isOpen, open: showModal, close } = useDisclosure();

  const toggleLike = async (reviewId: string) => {
    try {
      const result = await toggleReviewLike(reviewId);

      setReviews(prev =>
        prev.map(r =>
          r.id === reviewId
            ? {
                ...r,
                likedByMe: result.liked,
                likeCount: result.liked ? r.likeCount + 1 : r.likeCount - 1
              }
            : r
        )
      );
    } catch (error) {
      console.error("좋아요 토글 실패:", error);
      alert("좋아요 처리에 실패했습니다. 다시 시도해주세요.");
    }
  };

  useEffect(() => {
    setReviews(data);
  }, [data]);

  if (reviews.length === 0) {
    return (
      <div>
        <p className="text-body2 font-medium text-gray-400 text-center">
          등록된 리뷰가 없습니다.
        </p>
      </div>
    );
  }

  return (
    <>
      <DelayedLoader isLoading={isLoading} delay={1000}>
        <InfiniteScrollLoader />
      </DelayedLoader>
      <div className="mt-5 flex flex-col gap-5">
        {reviews.map(review => {
          const isEdit = editingReviewId === review.id;
          const isMyReview = user?.id === review.userId;
          // 본인 리뷰이거나 관리자면 메뉴 노출 (관리자는 삭제만 가능)
          const canManageReview = isMyReview || isAdmin(user?.role);

          return (
            <div key={review.id} className="pb-5 border-b border-gray-100">
              <div className="flex items-center jsutify-between mb-[10px]">
                <p className="flex flex-[1] gap-2 items-center text-body3 font-semibold text-gray-600">
                  {review.userNickname}
                  {isMyReview && (
                    <Label className="bg-red-100 text-red-500">내 리뷰</Label>
                  )}
                  <span className="font-medium text-gray-400">
                    {formatDate(review.createdAt)}
                  </span>
                </p>
                {canManageReview && (
                  <ActionDropdown
                    showModal={showModal}
                    reviewId={review.id}
                    setReviewId={setReviewId}
                    canEdit={isMyReview}
                    setIsEdit={() => {
                      setEditingReviewId(review.id);
                      setRating(review.rating);
                    }}
                  />
                )}
              </div>
              {isEdit ? (
                <ReviewRating
                  totalStars={5}
                  rating={rating}
                  setRating={setRating}
                />
              ) : (
                <StarRating rating={review.rating} />
              )}
              {isEdit ? (
                <EditContainer
                  reviewId={review.id}
                  bookId={bookId}
                  data={data}
                  setData={setData}
                  prevValue={review.content}
                  setEditingReviewId={setEditingReviewId}
                  rating={rating}
                  prevRating={review.rating}
                />
              ) : (
                <p className="mt-2 text-gray-700 text-body3 font-medium whitespace-pre-line">
                  {review.content}
                  {review.updatedAt && review.updatedAt !== review.createdAt && (
                    <span className="ml-2 my-[3.5px] text-body3 font-medium text-gray-400">
                      (수정됨)
                    </span>
                  )}
                </p>
              )}
              <div className="flex items-center gap-[12px] pt-[8px]">
                <div
                  className="flex items-center cursor-pointer hover:opacity-70 transition-opacity"
                  onClick={() => toggleLike(review.id)}
                >
                  <img
                    src={
                      review.likedByMe
                        ? getImagePath("/icon/ic_heart_black.svg")
                        : getImagePath("/icon/ic_heart.svg")
                    }
                    alt="좋아요"
                    width={16}
                    height={16}
                    className="mr-[2px]"
                  />
                  <span className="text-body3 font-medium text-gray-500">
                    좋아요 {review.likeCount}
                  </span>
                </div>
                <div className="flex items-center">
                  <img
                    src={getImagePath("/icon/ic_comment.svg")}
                    alt="댓글"
                    width={16}
                    height={16}
                    className="mr-[2px]"
                  />
                  <span className="text-body3 font-medium text-gray-500">
                    댓글 {review.commentCount}
                  </span>
                </div>
              </div>
            </div>
          );
        })}
      </div>
      <ReviewDeleteModal
        isOpen={isOpen}
        close={close}
        reviewId={reviewId}
        data={data}
        setData={setData}
        setTotalElements={setTotalElements}
        bookId={bookId}
      />
    </>
  );
}
