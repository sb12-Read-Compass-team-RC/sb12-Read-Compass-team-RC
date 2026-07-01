import Modal from "@/components/ui/Modal";

interface ReviewDetailDeleteModalProps {
    isOpen: boolean;
    isSubmitting: boolean;
    onClose: () => void;
    onDelete: () => void;
}

export default function ReviewDetailDeleteModal({
                                                    isOpen,
                                                    isSubmitting,
                                                    onClose,
                                                    onDelete
                                                }: ReviewDetailDeleteModalProps) {
    return (
        <Modal
            isDelete
            isOpen={isOpen}
            onClose={onClose}
            disabled={isSubmitting}
            buttonText="삭제"
            action={onDelete}
        >
            <p className="font-medium">리뷰를 삭제하시겠습니까?</p>
        </Modal>
    );
}