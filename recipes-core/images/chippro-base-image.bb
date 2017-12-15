DESCRIPTION = "Creates base chippro image."

# Base this image on core-image-minimal
# include recipes-core/images/core-image-minimal.bb
inherit core-image deploy

DEPENDS += "u-boot chip-tools-native sunxi-tools-native u-boot-mkimage-native android-tools-native coreutils-native"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PR = "r0"

ROOTFS_POSTPROCESS_COMMAND += "do_create_image; "

do_create_image() {
    echo "do_create_image called S='${S}' WORKDIR='${WORKDIR}' THISDIR='${THISDIR}'"

    cat >${WORKDIR}/flashboot.txt <<-EOF
	mw \${scriptaddr} 0x0
	sleep 4
	fastboot 0
	EOF

    cat >${WORKDIR}/runboot.txt <<-EOF
	bootdelay=2
	baudrate=115200
	preboot=
	arch=arm
	cpu=armv7
	board=sunxi
	board_name=sunxi
	soc=sunxi
	stdin=serial,usbkbd
	stdout=serial,vga
	stderr=serial,vga
	bootm_size=0xa000000
	kernel_addr_r=0x42000000
	fdt_addr_r=0x43000000
	ubipart=${UBI_PART}
	ubioffset=${UBI_ECC_HEADER}
	ubisuffixes=${UBI_BOOT_SUFFIXES}
	fsvolname=${UBI_BOOT_ROOTFS}
	fdtvolname=${UBI_BOOT_FDT}
	scriptaddr=0x43100000
	pxefile_addr_r=0x43200000
	ramdisk_addr_r=0x43300000
	dfu_alt_info_ram=kernel ram 0x42000000 0x1000000;fdt ram 0x43000000 0x100000;ramdisk ram 0x43300000 0x4000000
	console=ttyS0,115200
	boot_targets=fel ubi
	boot_one_ubi= \
		echo Test for ubi\${devnum}:\${fsvol}; \
		if ubifsmount ubi\${devnum}:\${fsvol} && ubifsload \${kernel_addr_r} boot/zImage && ubi read \${fdt_addr_r} \${fdtvol}; then \
			setenv bootargs "root=ubi\${devnum}:\${fsvol} rootfstype=ubifs ubi.mtd=UBI,\${ubioffset} \${extrabootargs}"; \
			echo Booting ubi\${devnum}:\${fsvol}/boot/zImage; \
			echo Using bootargs \${bootargs}; \
			bootz \${kernel_addr_r} - \${fdt_addr_r}; \
		fi
	bootcmd_fel= \
		if test -n \${fel_booted} && test -n \${fel_scriptaddr}; then \
			echo '(FEL boot)'; source \${fel_scriptaddr};
		fi
	bootcmd_ubi= \
		ubi part UBI \${ubioffset}; \
		if test -z "\${ubisuffixes}"; then \
			setenv fsvol "\${fsvolname}"; \
			setenv fdtvol "{fdtvolname}"; \
			run boot_one_ubi; \
		else \
			for suffix in \${ubisuffixes}; do \
				setenv fsvol "\${fsvolname}\${suffix}"; \
				setenv fdtvol "\${fdtvolname}\${suffix}"; \
				run boot_one_ubi; \
			done; \
		fi
	bootcmd= \
		for target in \${boot_targets}; do \
			run bootcmd_\${target}; \
		done
	EOF

    # Replace tabs with spaces in the above file (tabs were there to make it easier to read)
    sed -i -e "s/[[:space:]]\+/ /g"				${WORKDIR}/flashboot.txt
    sed -i -e "s/^\(.*\\)[[:space:]]*=[[:space:]]/\1=/g"	${WORKDIR}/flashboot.txt

    sed -i -e "s/[[:space:]]\+/ /g"				${WORKDIR}/runboot.txt
    sed -i -e "s/^\(.*\\)[[:space:]]*=[[:space:]]/\1=/g"	${WORKDIR}/runboot.txt

    chip-create-nand-images.sh ${DEPLOY_DIR_IMAGE} ${B} ${NAND_ERASE_BLOCK_SIZE} ${NAND_PAGE_SIZE} ${NAND_OOB_SIZE}

    # install ${B}/sunxi-spl.bin ${DEPLOY_DIR_IMAGE}

    # Create boot.scr
    mkimage -A arm -T script -C none -n "Flash" -d "${WORKDIR}/flashboot.txt" "${B}/boot.scr"
    install ${B}/boot.scr ${DEPLOY_DIR_IMAGE}

    # Strip out the mtd information and add it to the boot text script
    grep "^mtd" ${DEPLOY_DIR_IMAGE}/${UBOOT_ENV_NAME}.base.txt >>${DEPLOY_DIR_IMAGE}/${UBOOT_ENV_NAME}.txt

    # Extract bootargs and rename
    sed -n -e "s/bootargs=\(.*\)/extrabootargs=\1/p" <${DEPLOY_DIR_IMAGE}/${UBOOT_ENV_NAME}.base.txt >>${DEPLOY_DIR_IMAGE}/${UBOOT_ENV_NAME}.txt

    # mkenvimage -s ${ENV_IMAGE_SIZE} -o ${DEPLOY_DIR_IMAGE}/${UBOOT_ENV_NAME} ${DEPLOY_DIR_IMAGE}/${UBOOT_ENV_NAME}.txt

    mkenvimage -s ${ENV_IMAGE_SIZE} -o ${B}/${UBOOT_ENV_NAME} ${DEPLOY_DIR_IMAGE}/${UBOOT_ENV_NAME}.txt
    install ${B}/${UBOOT_ENV_NAME} ${DEPLOY_DIR_IMAGE}
}
