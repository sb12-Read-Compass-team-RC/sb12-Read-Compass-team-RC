import { useAuthGuard } from "@/hooks/auth/useAuthRedirect";
import LoadingScreen from "@/components/common/LoadingScreen";
import PopularBooks from "./components/sections/PopularBooks";
import PopularReviews from "./components/sections/PopularReviews";
import UserRanking from "./components/sections/UserRanking";
import clsx from "clsx";

export default function Home() {
  const { shouldShowContent } = useAuthGuard();

  if (!shouldShowContent) {
    return <LoadingScreen />;
  }
  return (
    <div className="pt-[50px] pb-[80px]">
      <div className={clsx("flex gap-[32px]", "max-lg:flex-col")}>
        <div className="flex-1">
          <div className="flex flex-col gap-[60px]">
            <PopularBooks />

            <div className="border-t border-gray-100"></div>

            <PopularReviews />
          </div>
        </div>

        <div className="w-[228px]">
          <UserRanking />
        </div>
      </div>
    </div>
  );
}
