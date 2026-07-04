import { useAuthGuard } from "@/hooks/auth/useAuthRedirect";
import LoadingScreen from "@/components/common/LoadingScreen";
import { useParams } from "react-router-dom";
import BookThumbnail from "./components/overview/BookThumbnail";
import BookInfo from "./components/overview/BookInfo";
import OverviewContainer from "./components/overview/OverviewContainer";
import ReviewContainer from "./components/review/ReviewContainer";
import ReviewForm from "./components/review/ReviewForm";
import ReviewList from "./components/review/ReviewList";
import clsx from "clsx";
import { useState } from "react";

export default function BookDetailPage() {
  const { id: paramsId } = useParams();
  const id = String(paramsId);

  const { shouldShowContent } = useAuthGuard();
  const [bookRefreshKey, setBookRefreshKey] = useState(0);

  const refreshBookDetail = () => {
    setBookRefreshKey(prev => prev + 1);
  };

  if (!shouldShowContent) {
    return <LoadingScreen />;
  }

  return (
      <div
          className={clsx(
              "pt-[50px] pb-[150px] h-[inherit] min-h-[inherit] flex flex-col gap-[40px]",
              "max-lg900:pt-0"
          )}
      >
        <OverviewContainer id={id} refreshKey={bookRefreshKey}>
          {({ data }) => (
              <>
                <BookThumbnail data={data} />
                <BookInfo id={id} data={data} />
              </>
          )}
        </OverviewContainer>

        <ReviewContainer id={id}>
          {({ data, setData, isLoading, totalElements, setTotalElements }) => (
              <>
                <ReviewForm
                    data={data}
                    setData={setData}
                    totalElements={totalElements}
                    setTotalElements={setTotalElements}
                    bookId={id}
                    onBookDetailRefresh={refreshBookDetail}
                />
                <ReviewList
                    data={data}
                    setData={setData}
                    isLoading={isLoading}
                    setTotalElements={setTotalElements}
                    bookId={id}
                    onBookDetailRefresh={refreshBookDetail}
                />
              </>
          )}
        </ReviewContainer>
      </div>
  );
}