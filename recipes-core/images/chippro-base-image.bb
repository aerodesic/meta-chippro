

# Base this image on core-image-minimal
include recipes-core/images/core-image-minimal.bb
# inherit core-image deploy

DEPENDS += "u-boot chip-tools-native sunxi-tools-native u-boot-mkimage-native android-tools-native coreutils-native"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PR = "r0"

ROOTFS_POSTPROCESS_COMMAND += "do_create_image; "

do_create_image() {

    echo "do_create_image: DEPLOY_DIR_IMAGE = '${DEPLOY_DIR_IMAGE}'"

    chip-create-nand-images.sh \
	${B}/${UBOOT_INSTALL_FILE} \
	${DEPLOY_DIR_IMAGE}/${UBOOT_BINARY} \
	${B}/${SPL_INSTALL_FILE} \
	${DEPLOY_DIR_IMAGE}/sunxi-spl.bin \
	${NAND_ERASE_BLOCK_SIZE} \
	${NAND_PAGE_SIZE} \
	${NAND_OOB_SIZE}

    # Create the uboot script for flashing the unit.
    UBOOT_SIZE="$(stat --printf='%s' ${B}/${UBOOT_INSTALL_FILE})"

    echo "UBOOT_SIZE (size of '${B}/${UBOOT_INSTALL_FILE}' is ${UBOOT_SIZE}"

    tr --delete "\t" <<-EOF | tr -s " " " " >${B}/${UBOOT_SCRIPT_BASE_NAME}.txt
	setenv `fgrep mtdparts= ${ENV_IMAGE}.txt`
	setenv bootcmd 'echo Enter fastboot;fastboot 0'
	setenv fel_booted 0
	setenv stdout serial
	setenv stderr serial
	nand erase.chip
	nand write.raw.noverify   ${SPL_MEM_ADDRESS} spl           ${NAND_OOB_SIZE}
	nand write.raw.noverify   ${SPL_MEM_ADDRESS} spl-backup    ${NAND_OOB_SIZE}
	nand write                ${UBOOT_MEM_ADDR}  uboot         ${UBOOT_SIZE}
	nand write                ${UBOOT_MEM_ADDR}  uboot-backup  ${UBOOT_SIZE}
	echo Going to fastboot mode
	fastboot 0
	while true; do; sleep 10; done
	EOF

    mkimage -A arm -T script -C none -n "flash ${MACHINE}" -d ${B}/${UBOOT_SCRIPT_BASE_NAME}.txt ${B}/${UBOOT_SCRIPT_BASE_NAME}.bin

    install ${B}/${SPL_INSTALL_FILE} ${DEPLOY_DIR_IMAGE}
    install ${B}/${UBOOT_INSTALL_FILE} ${DEPLOY_DIR_IMAGE}
    install ${B}/${UBOOT_SCRIPT_BASE_NAME}.* ${DEPLOY_DIR_IMAGE}
}

