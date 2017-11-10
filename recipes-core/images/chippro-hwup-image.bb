# Base this image on core-image-minimal
include recipes-core/images/core-image-minimal.bb

DEPENDS+="android-tools-native"

NAND_ERASE_BLOCK_SIZE = "262144"


do_post_process_images() {
    # Create .ubi.sparse of this file
    cd ${IMGDEPLOYDIR}
    img2simg ${IMAGE_NAME}${IMAGE_NAME_SUFFIX}.ubi ${IMAGE_NAME}${IMAGE_NAME_SUFFIX}.ubi.sparse ${NAND_ERASE_BLOCK_SIZE}
    rm -f ${IMAGE_BASENAME}-${MACHINE}.ubi.sparse
    ln -sf ${IMAGE_NAME}${IMAGE_NAME_SUFFIX}.ubi.sparse ${IMAGE_BASENAME}-${MACHINE}.ubi.sparse
    # Fix the blocksize of the zimage dtb
}

do_image_ubi[postfuncs] += "do_post_process_images"

