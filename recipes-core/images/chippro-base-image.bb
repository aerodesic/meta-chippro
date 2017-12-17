# Base this image on core-image-minimal
include recipes-core/images/core-image-minimal.bb
# inherit core-image deploy

DEPENDS += "u-boot chip-tools-native sunxi-tools-native u-boot-mkimage-native android-tools-native coreutils-native"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PR = "r0"

ROOTFS_POSTPROCESS_COMMAND += "do_create_image; "

UBI_ECC_HEADER ?= "0"

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


    tr --delete "\t" << EOF | tr -s " " " " >${B}/${UBOOT_SCRIPT_BASE_NAME}.txt
	$(fgrep mtdids=   ${DEPLOY_DIR_IMAGE}/${UBOOT_ENV_NAME}.txt | sed -e 's/\([a-zA-Z_]*\)=/setenv \1 /')
	$(fgrep mtdparts= ${DEPLOY_DIR_IMAGE}/${UBOOT_ENV_NAME}.txt | sed -e 's/\([a-zA-Z_]*\)=/setenv \1 /')
	setenv fel_booted 0
	setenv stdout serial
	setenv stderr serial
	nand erase.chip
	nand write.raw.noverify   ${SPL_MEM_ADDR}    spl           $(printf "%x" ${NAND_OOB_SIZE})
	nand write.raw.noverify   ${SPL_MEM_ADDR}    spl-backup    $(printf "%x" ${NAND_OOB_SIZE})
	nand write                ${UBOOT_MEM_ADDR}  uboot         $(printf "%x" ${UBOOT_SIZE})
	nand write                ${UBOOT_MEM_ADDR}  uboot-backup  $(printf "%x" ${UBOOT_SIZE})
	setenv boot_suffixes '${UBI_BOOT_SUFFIXES}'
	setenv boot_one_image '\
		ubifsmount ubi0:\${bootvol}; \
		ubifsload \${fdt_addr_r} /boot/${KERNEL_DEVICETREE}; \
		ubifsload \${kernel_addr_r} /boot/zImage; \
		setenv bootargs root=ubi0:\${bootvol} rootfstype=ubifs rw ubi.fm_autoconvert=1 ubi.mtd=UBI,${UBI_ECC_HEADER} quiet; \
		bootz \${kernel_addr_r} - \${fdt_addr_r}'
	setenv bootcmd '\
		if test -n \${fel_booted} && test -n \${scriptaddr}; then \
			echo (FEL boot); \
			source \${scriptaddr}; \
		fi; \
		ubi part UBI ${UBI_ECC_HEADER}; \
		run boot_image'
	setenv boot_image '\
		if test -z "\${boot_suffixes}"; then \
			bootvol=""; \
			run boot_image; \
		else \
			for suffix in \${boot_suffixes}; do \
				bootvol=${UBIVOL_NAME_rootfs}\${suffix}; \
				run boot_one_image; \
			done; \
		fi'
	saveenv
	echo Going to fastboot mode
	fastboot 0
	while true; do; sleep 10; done
EOF

    mkimage -A arm -T script -C none -n "flash ${MACHINE}" -d ${B}/${UBOOT_SCRIPT_BASE_NAME}.txt ${B}/${UBOOT_SCRIPT_BASE_NAME}.bin

    install ${B}/${SPL_INSTALL_FILE} ${DEPLOY_DIR_IMAGE}
    install ${B}/${UBOOT_INSTALL_FILE} ${DEPLOY_DIR_IMAGE}
    install ${B}/${UBOOT_SCRIPT_BASE_NAME}.bin ${DEPLOY_DIR_IMAGE}
    install ${B}/${UBOOT_SCRIPT_BASE_NAME}.txt ${DEPLOY_DIR_IMAGE}
}

