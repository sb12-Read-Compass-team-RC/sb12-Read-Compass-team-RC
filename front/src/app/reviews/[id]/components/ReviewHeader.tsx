import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import StarRating from "@/components/common/StarRating";
import LoadingScreen from "@/components/common/LoadingScreen";
import {
  deleteReview,
  getReviewDetail,
  putReview,
  toggleReviewLike
} from "@/api/reviews";
import { useTooltipStore } from "@/store/tooltipStore";
import { useAuthStore } from "@/store/authStore";
import { isAdmin } from "@/utils/authRole";
import ActionDropdown from "@/app/books/[id]/components/review/ActionDropdown";
import type { Review } from "@/types/reviews";
import getImagePath from "@/constants/images.ts";
import ReviewDetailEditForm from "./ReviewDetailEditForm";
import ReviewDetailDeleteModal from "./ReviewDetailDeleteModal";

interface ReviewHeaderProps {
  reviewId: string;
  commentCount?: number;
}

export default function ReviewHeader({
                                       reviewId,
                                       commentCount
                                     }: ReviewHeaderProps) {
  const navigate = useNavigate();

  const [review, setReview] = useState<Review | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [isEdit, setIsEdit] = useState(false);
  const [isDeleteOpen, setIsDeleteOpen] = useState(false);
  const [selectedReviewId, setSelectedReviewId] = useState("");

  const { showTooltip } = useTooltipStore();
  const { user } = useAuthStore();

  useEffect(() => {
    const fetchReview = async () => {
      if (!reviewId) return;

      try {
        setIsLoading(true);
        setError(null);

        const reviewData = await getReviewDetail(reviewId);
        setReview(reviewData);
      } catch (error) {
        console.error("리뷰 조회 실패:", error);
        setError("리뷰를 불러오는데 실패했습니다.");
      } finally {
        setIsLoading(false);
      }
    };

    fetchReview();
  }, [reviewId]);

  const handleToggleLike = async () => {
    if (!review) return;

    try {
      const result = await toggleReviewLike(review.id);

      setReview(prev => {
        if (!prev) return prev;

        return {
          ...prev,
          likedByMe: result.liked,
          likeCount: result.liked ? prev.likeCount + 1 : prev.likeCount - 1
        };
      });
    } catch (error) {
      console.error("좋아요 토글 실패:", error);
      showTooltip("좋아요 처리에 실패했습니다.", "error");
    }
  };

  const handleUpdateReview = async (content: string, rating: number) => {
    if (!review) return;

    try {
      setIsSubmitting(true);

      const updatedReview = await putReview(review.id, {
        content,
        rating
      });

      setReview(updatedReview);
      setIsEdit(false);

      showTooltip("리뷰 수정이 완료되었습니다!");
    } catch (error) {
      console.error("리뷰 수정 실패:", error);
      showTooltip("리뷰 수정에 실패했습니다.", "error");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteReview = async () => {
    if (!selectedReviewId) return;

    try {
      setIsSubmitting(true);

      await deleteReview(selectedReviewId);

      setIsDeleteOpen(false);
      showTooltip("리뷰를 정상적으로 삭제하였습니다!");

      // 리뷰 상세 전용 삭제 후 이동 위치
      navigate("/reviews");
    } catch (error) {
      console.error("리뷰 삭제 실패:", error);
      showTooltip("리뷰 삭제에 실패했습니다.", "error");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return <LoadingScreen />;
  }

  const isMyReview = user?.id === review?.userId;
  const canManageReview = !!review && (isMyReview || isAdmin(user?.role));

  return (
      <>
        <div className="flex gap-6">
          <div className="w-[118px] h-[178px] min-w-[118px] relative border rounded-lg overflow-hidden">
            <img
                src={review?.bookThumbnailUrl || getImagePath("/books/imgError.png")}
                alt={review?.bookTitle || "도서 이미지"}
                className="rounded-lg"
                onError={e => {
                  const target = e.target as HTMLImageElement;
                  target.src = getImagePath("/books/imgError.png");
                }}
            />
          </div>

          <div className="flex flex-col gap-[10px] flex-1">
            <div className="flex items-start justify-between gap-4">
              <div className="text-body4 font-medium text-gray-500 underline decoration-solid underline-offset-0 decoration-0 line-clamp-2">
                {review?.bookTitle ||
                    (error
                        ? "리뷰를 불러오는데 실패했습니다."
                        : "리뷰를 찾을 수 없습니다.")}
              </div>

              {canManageReview && !isEdit && (
                  <ActionDropdown
                      showModal={() => setIsDeleteOpen(true)}
                      reviewId={review.id}
                      setReviewId={setSelectedReviewId}
                      canEdit={isMyReview}
                      setIsEdit={() => setIsEdit(true)}
                  />
              )}
            </div>

            <div className="flex items-center gap-[6px] py-[3.5px]">
            <span className="text-body3 font-semibold text-gray-600">
              {review?.userNickname || "-"}
            </span>

              <span className="text-body3 font-medium text-gray-400">
              {review?.createdAt
                  ? new Date(review.createdAt).toLocaleDateString()
                  : "-"}
            </span>
            </div>

            {isEdit && review ? (
                <ReviewDetailEditForm
                    initialContent={review.content}
                    initialRating={review.rating}
                    isSubmitting={isSubmitting}
                    onSubmit={handleUpdateReview}
                    onCancel={() => setIsEdit(false)}
                />
            ) : (
                <>
                  <div className="flex flex-col flex-[1]">
                    <div className="mb-[8px]">
                      <StarRating rating={review?.rating || 0} size={18} />
                    </div>

                    <div className="text-body2 font-medium text-gray-800 overflow-hidden whitespace-pre-line">
                      {review?.content || "-"}
                      {review?.updatedAt &&
                          review.updatedAt !== review.createdAt && (
                              <span className="ml-2 text-body3 font-medium text-gray-400">
                        (수정됨)
                      </span>
                          )}
                    </div>
                  </div>

                  <div className="flex items-center gap-[12px] pt-[8px]">
                    <div
                        className="flex items-center cursor-pointer hover:opacity-70 transition-opacity"
                        onClick={review ? handleToggleLike : undefined}
                    >
                      <img
                          src={
                            review?.likedByMe
                                ? getImagePath("/icon/ic_heart_black.svg")
                                : getImagePath("/icon/ic_heart.svg")
                          }
                          alt="좋아요"
                          width={16}
                          height={16}
                          className="mr-[2px] w-4 h-4"
                      />

                      <span className="text-body3 font-medium text-gray-500">
                    좋아요 {review?.likeCount || 0}
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
                    댓글{" "}
                        {commentCount !== undefined
                            ? commentCount
                            : review?.commentCount || 0}
                  </span>
                    </div>
                  </div>
                </>
            )}
          </div>
        </div>

        <ReviewDetailDeleteModal
            isOpen={isDeleteOpen}
            isSubmitting={isSubmitting}
            onClose={() => setIsDeleteOpen(false)}
            onDelete={handleDeleteReview}
        />
      </>
  );
}