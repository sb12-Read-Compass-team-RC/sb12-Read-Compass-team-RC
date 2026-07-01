import { useAuthGuard } from "@/hooks/auth/useAuthRedirect";
import LoadingScreen from "@/components/common/LoadingScreen";
import PageHead from "../components/bookForm/PageHead";
import FormContainer from "../components/bookForm/FormContainer";
import FormFields from "../components/bookForm/FormFields";
import clsx from "clsx";

export default function AddBookPage() {
  const { shouldShowContent } = useAuthGuard();

  if (!shouldShowContent) {
    return <LoadingScreen />;
  }

  return (
    <div className={clsx("pt-[50px]", "max-md:pb-[150px]")}>
      <PageHead mode="add" />
      <FormContainer>
        <FormFields />
      </FormContainer>
    </div>
  );
}
