import { useFormContext } from "react-hook-form";
import FormInputsContainer from "./FormInputsContainer";
import ImgUploadContainer from "./ImgUploadContainer";
import ButtonContainer from "./ButtonContainer";
import { BookFormValues } from "@/schemas/bookFormSchema";
import { useState } from "react";
import {useNavigate} from "react-router-dom";
import { BookResponse, postBook, putBook } from "@/api/books";
import { useTooltipStore } from "@/store/tooltipStore";
import axios from "axios";
import clsx from "clsx";
import getImagePath from "@/constants/images.ts";

interface FormFieldsProps {
  id?: string;
  data?: BookResponse;
  isEdit?: boolean;
}

export default function FormFields({
  id,
  data,
  isEdit = false
}: FormFieldsProps) {
  const {
    register,
    control,
    setValue,
    setError,
    trigger,
    formState,
    watch,
    handleSubmit
  } = useFormContext<BookFormValues>();

  const formMethods = {
    register,
    control,
    setValue,
    setError,
    trigger,
    watch,
    formState
  };

  const [imageFile, setImageFile] = useState<File | null>(null);
  const [isFetchIsbnLoading, setIsFetchIsbnLoading] = useState(false);

  const navigate = useNavigate();

  const watchedThumbnailUrl = watch("thumbnailUrl");
  const thumbnailValue = watchedThumbnailUrl || data?.thumbnailUrl || "";
  const tooltipErrorImg = getImagePath("/icon/ic_exclamation-circle.svg");

  const showTooltip = useTooltipStore(state => state.showTooltip);
  const { isDirty, isValid, isSubmitting } = formState;

  const isFocusDisabled = isFetchIsbnLoading || isSubmitting;
  const isSubmitDisabled =
    !isDirty || isFetchIsbnLoading || !isValid || isSubmitting;

  const onSubmit = async (data: BookFormValues) => {
    const formData = new FormData();

    const bookData = {
      isbn: data.isbn,
      title: data.title,
      author: data.author,
      publisher: data.publisher,
      publishedDate: data.publishedDate,
      description: data.description,
      category: data.category,
      thumbnailUrl: data.thumbnailUrl
    };

    formData.append(
      "bookData",
      new Blob([JSON.stringify(bookData)], { type: "application/json" })
    );

    if (imageFile) {
      formData.append("thumbnailImage", imageFile);
    }

    try {
      if (id && isEdit) {
        await putBook(id, formData);
        showTooltip("도서 수정이 완료되었습니다!");
        navigate(`/books/${id}`);
      } else {
        await postBook(formData);
        showTooltip("도서 등록이 완료되었습니다!");
        navigate("/books");
      }
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        const status = error.response?.status;
        const responseData = error.response?.data;
        const codeName = String(responseData?.codeName ?? responseData?.code ?? "");
        const errorMessage = String(responseData?.message ?? error.message ?? "");
        const detailText = JSON.stringify(responseData?.details ?? responseData ?? {});
        const duplicateCheckText = `${codeName} ${errorMessage} ${detailText}`;

        const isDuplicateIsbnError =
          status === 409 ||
          status === 405 ||
          codeName.includes("DUPLICATE_ISBN") ||
          codeName.includes("DUPLICATE_BOOK") ||
          (duplicateCheckText.includes("ISBN") &&
            (duplicateCheckText.includes("중복") ||
              duplicateCheckText.includes("이미") ||
              duplicateCheckText.includes("등록") ||
              duplicateCheckText.includes("존재") ||
              duplicateCheckText.includes("duplicate") ||
              duplicateCheckText.includes("Duplicate"))) ||
          (status === 400 &&
            (duplicateCheckText.includes("중복") ||
              duplicateCheckText.includes("이미") ||
              duplicateCheckText.includes("존재")));

        if (isDuplicateIsbnError) {
          setError(
            "isbn",
            {
              type: "manual",
              message: "이미 등록된 ISBN입니다."
            },
            { shouldFocus: true }
          );
        } else if (status === 413) {
          showTooltip("파일 용량이 초과되었습니다.", tooltipErrorImg);
        } else if (status === 400) {
          showTooltip(
            "입력하신 정보를 확인 후 다시 시도해주세요.",
            tooltipErrorImg
          );
        } else if (status) {
          showTooltip(
            `알 수 없는 오류가 발생했습니다. (코드: ${status})`,
            tooltipErrorImg
          );
        } else {
          // 네트워크 설정 오류
          showTooltip(
            "서버 응답이 없습니다. 네트워크 상태를 확인해주세요.",
            tooltipErrorImg
          );
        }
      } else if (error instanceof Error && error.message.includes("ISBN")) {
        setError(
          "isbn",
          {
            type: "manual",
            message: "이미 등록된 ISBN입니다."
          },
          { shouldFocus: true }
        );
      }
      console.error("도서 등록 실패:", error);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <fieldset disabled={isSubmitting}>
        <div
          className={clsx("flex gap-10 mt-[30px]", "max-md:flex-col-reverse")}
        >
          <FormInputsContainer
            isEdit={isEdit}
            formMethods={formMethods}
            isFocusDisabled={isFocusDisabled}
            setIsFetchIsbnLoading={setIsFetchIsbnLoading}
            isSubmitting={isSubmitting}
          />
          <ImgUploadContainer
            imageFile={imageFile}
            setImageFile={setImageFile}
            thumbnailValue={thumbnailValue}
            setValue={setValue}
          />
        </div>
        <ButtonContainer
          id={id}
          isSubmitDisabled={isSubmitDisabled}
          isSubmitting={isSubmitting}
          isEdit={isEdit}
        />
      </fieldset>
    </form>
  );
}
