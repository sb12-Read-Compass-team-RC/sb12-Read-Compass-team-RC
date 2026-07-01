import { useAuthGuard } from "@/hooks/auth/useAuthRedirect";
import LoadingScreen from "@/components/common/LoadingScreen";
import PageHead from "./components/PageHead";
import BookSearchSection from "./components/BookSearchSection";
import { useEffect, useState } from "react";
import ContentsList from "./components/ContentsList";
import { Book, BooksParams, getBooks } from "@/api/books";
import { useInfiniteScroll } from "@/hooks/common/useInfiniteScroll";
import EmptyList from "@/components/common/EmptyList";
import useResponsiveLimit from "@/hooks/book/useResponsiveLimit";

export default function BooksPage() {
  const [orderBy, setOrderBy] = useState<
    "title" | "publishedDate" | "rating" | "reviewCount"
  >("title");
  const [direction, setDirection] = useState<"ASC" | "DESC">("DESC");
  const [keyword, setKeyword] = useState("");
  const [category, setCategory] = useState("");
  const [booksData, setBooksData] = useState<Book[]>([]);
  const limit = useResponsiveLimit("bookList");

  const { isLoading, setCursor, setAfter, setIsLoading, resetInfiniteScroll } =
    useInfiniteScroll<Book, BooksParams>({
      initialParams: { orderBy, direction, keyword, category, limit },
      fetcher: getBooks,
      setData: setBooksData
    });

  useEffect(() => {
    const fetchBook = async () => {
      setIsLoading(true);

      try {
        const response = await getBooks({
          orderBy,
          direction,
          keyword,
          category,
          limit
        });
        setBooksData(response.content);
        setCursor(response.nextCursor);
        setAfter(response.nextAfter);
      } catch (err) {
        console.error("도서 조회 실패:", err);
      } finally {
        setIsLoading(false);
      }
    };

    resetInfiniteScroll();
    setBooksData([]);
    fetchBook();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [orderBy, direction, keyword, category]);

  const { shouldShowContent } = useAuthGuard();

  if (!shouldShowContent) {
    return <LoadingScreen />;
  }

  return (
    <div className="pt-[50px] pb-[80px] h-[inherit] min-h-[inherit] flex flex-col">
      <PageHead />
      <BookSearchSection
        orderBy={orderBy}
        direction={direction}
        category={category}
        onSearch={setKeyword}
        onCategoryChange={setCategory}
        onOrderByChange={setOrderBy}
        onDirectionChange={setDirection}
      />
      {booksData.length === 0 && !isLoading ? (
        <EmptyList keyword={keyword} />
      ) : (
        <ContentsList booksData={booksData} isLoading={isLoading} />
      )}
    </div>
  );
}
