# Base this image on core-image-minimal
include recipes-core/images/core-image-minimal.bb

LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://${WORKDIR}/LICENSE;md5=b234ee4d69f5fce4486a80fdaf4a4263"

SRC_URI = " \
	file://uboot.script \
	"

S = "${WORKDIR}/git"

DEPENDS+="android-tools-native"

do_post_process_images() {
    # Create .ubi.sparse of this file
    cd ${IMGDEPLOYDIR}
    img2simg ${IMAGE_NAME}${IMAGE_NAME_SUFFIX}.ubi ${IMAGE_NAME}${IMAGE_NAME_SUFFIX}.ubi.sparse ${NAND_ERASE_BLOCK_SIZE}
    rm -f ${IMAGE_BASENAME}-${MACHINE}.ubi.sparse
    ln -sf ${IMAGE_NAME}${IMAGE_NAME_SUFFIX}.ubi.sparse ${IMAGE_BASENAME}-${MACHINE}.ubi.sparse

    mkimage -A arm -T script -C none -n "Flash" -d "${WORKDIR}/uboot.script" "${WORKDIR}/boot.scr"
}

do_image_ubi[postfuncs] += "do_post_process_images"

