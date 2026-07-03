import { BookResponse } from "@/api/books";
import { useState } from "react";
import getImagePath from "@/constants/images.ts";

export default function BookThumbnail({ data }: { data: BookResponse | null }) {
  const [imgErrors, setImgErrors] = useState<Record<string, boolean>>({});

  const hasThumbnail = data?.thumbnailUrl && !imgErrors[data.id];

  return (
      <div className="w-[400px] h-[600px] flex-shrink-0 flex items-start justify-center">
        {hasThumbnail ? (
            <img
                src={data.thumbnailUrl}
                alt={data.title || "thumbnail"}
                onError={() => setImgErrors(prev => ({ ...prev, [data.id]: true }))}
                className="max-w-full max-h-full w-auto h-auto object-contain border border-gray-200 rounded-xl"
            />
        ) : (
            <img
                src={getImagePath("/books/imgError.png")}
                alt="이미지 없음"
                className="max-w-full max-h-full w-auto h-auto object-contain border border-gray-200 rounded-xl"
            />
        )}
      </div>
  );
}