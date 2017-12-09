# Append conversion of ubi to sparse
# Base this image on core-image-minimal
include recipes-core/images/core-image-minimal.bb

DEPENDS += "chip-tools-native sunxi-tools-native u-boot-mkimage-native android-tools-native"

ROOTFS_POSTPROCESS_COMMAND += "do_create_image; "

do_create_image() {
#   echo chip-create-nand-images.sh ${DEPLOY_DIR_IMAGE} ${NAND_ERASE_BLOCK_SIZE} ${SUNXI_NAND_PAGE_SIZE} ${SUNXI_NAND_OOB_SIZE}
   chip-create-nand-images.sh ${DEPLOY_DIR_IMAGE} ${NAND_ERASE_BLOCK_SIZE} ${SUNXI_NAND_PAGE_SIZE} ${SUNXI_NAND_OOB_SIZE}
}

