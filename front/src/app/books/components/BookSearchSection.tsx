import { memo } from "react";
import Selectbox from "@/components/ui/Selectbox";
import SearchBar from "@/components/ui/SearchBar";
import {
  BOOK_CATEGORY_FILTER_OPTIONS,
  BOOKS_ORDERBY,
  SORT_DIRECTION
} from "@/constants/selectOptions";
import clsx from "clsx";

type BookOrderBy = "title" | "publishedDate" | "rating" | "reviewCount";

interface BookSearchSectionProps {
  orderBy: BookOrderBy;
  direction: "ASC" | "DESC";
  category: string;
  onSearch: (value: string) => void;
  onCategoryChange: (value: string) => void;
  onOrderByChange: (value: BookOrderBy) => void;
  onDirectionChange: (value: "ASC" | "DESC") => void;
}

const BookSearchSection = memo(function BookSearchSection({
  orderBy,
  direction,
  category,
  onSearch,
  onCategoryChange,
  onOrderByChange,
  onDirectionChange
}: BookSearchSectionProps) {
  return (
    <div
      className={clsx(
        "flex justify-between items-center mb-[30px] gap-4",
        "max-xs650:flex-col max-xs650:items-start"
      )}
    >
      <div className="flex-[1] max-xs650:w-full">
        <SearchBar
          onSearch={onSearch}
          placeholder="제목, 저자, ISBN, 카테고리로 검색해보세요."
        />
      </div>

      <div className="flex items-center gap-2 max-xs650:w-full max-xs650:flex-wrap">
        <div className="w-[160px] max-xs650:flex-[1]">
          <Selectbox
            options={BOOK_CATEGORY_FILTER_OPTIONS}
            value={category}
            onChange={onCategoryChange}
          />
        </div>
        <Selectbox
          options={BOOKS_ORDERBY}
          value={orderBy}
          onChange={onOrderByChange}
        />
        <Selectbox
          options={SORT_DIRECTION}
          value={direction}
          onChange={onDirectionChange}
        />
      </div>
    </div>
  );
});

export default BookSearchSection;
