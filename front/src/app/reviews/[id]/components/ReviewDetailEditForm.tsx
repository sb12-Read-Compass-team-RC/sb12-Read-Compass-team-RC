import { FormEvent, useState } from "react";
import Button from "@/components/common/Buttons/Button";
import { textareaStyle } from "@/app/books/styles";
import ReviewRating from "@/app/books/[id]/components/review/ReviewRating";
import { useTooltipStore } from "@/store/tooltipStore";

interface ReviewDetailEditFormProps {
    initialContent: string;
    initialRating: number;
    isSubmitting: boolean;
    onSubmit: (content: string, rating: number) => Promise<void>;
    onCancel: () => void;
}

export default function ReviewDetailEditForm({
                                                 initialContent,
                                                 initialRating,
                                                 isSubmitting,
                                                 onSubmit,
                                                 onCancel
                                             }: ReviewDetailEditFormProps) {
    const [content, setContent] = useState(initialContent);
    const [rating, setRating] = useState(initialRating);

    const { showTooltip } = useTooltipStore();

    const isDirty =
        content.trim() !== "" &&
        (content !== initialContent || rating !== initialRating);

    const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        if (!content.trim()) {
            showTooltip("리뷰 내용을 입력해주세요.", "error");
            return;
        }

        await onSubmit(content, rating);
    };

    return (
        <form onSubmit={handleSubmit} className="flex flex-col gap-3">
            <ReviewRating totalStars={5} rating={rating} setRating={setRating} />

            <textarea
                value={content}
                onChange={e => setContent(e.target.value)}
                className={textareaStyle}
                placeholder="리뷰를 수정해주세요."
                maxLength={500}
            />

            <div className="flex justify-end gap-[12px]">
                <Button type="button" variant="secondary" onClick={onCancel}>
                    취소
                </Button>

                <Button variant="primary" disabled={!isDirty || isSubmitting}>
                    {isSubmitting ? (
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-gray-50 mx-auto" />
                    ) : (
                        "등록"
                    )}
                </Button>
            </div>
        </form>
    );
}