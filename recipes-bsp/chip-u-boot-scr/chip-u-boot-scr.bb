SUMMARY = "U-boot boot scripts for CHIPPRO boards"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

inherit deploy

DEPENDS = "u-boot-mkimage-native android-tools-native"

SRC_URI = "file://uboot.script"

SCRIPTADDR            = "0x43100000"

UBI_SIZE              = "0x0A000000"
LED_I2C_CHIP          = "0x34"
LED_I2C_ADDR          = "0x93"
# OOB_SIZE              = "1664"
OOB_SIZE              = "256"

VID                   = "-i 0x1f3a"

do_compile[depends] += "u-boot-chip:do_deploy"

do_compile() {
    echo WORKDIR=${WORKDIR}
    echo DEPLOY_DIR_IMAGE '${DEPLOY_DIR_IMAGE}'
    echo DEPLOYDIR '${DEPLOYDIR}'
    echo SPL_ECC_BINARY '${SPL_ECC_BINARY}'
    echo SPL_BINARY '${SPL_BINARY}'
    echo UBOOT_BINARY '${UBOOT_BINARY}'
    echo MACHINE '${MACHINE}'
    echo IMAGE_NAME '${IMAGE_NAME}'

    echo ---------------------------------------------
    ls -l ${WORKDIR}
    echo ---------------------------------------------

    PADDED_SPL_SIZE_BLOCKS=$(stat --dereference --printf="%s" "${DEPLOY_DIR_IMAGE}/${SPL_ECC_BINARY}")
    PADDED_SPL_SIZE_BLOCKS=$(expr $PADDED_SPL_SIZE_BLOCKS / \( ${CHIP_UBI_PAGE_SIZE} + ${OOB_SIZE} \))
    PADDED_SPL_SIZE_BLOCKS=$(echo $PADDED_SPL_SIZE_BLOCKS | xargs printf "0x%08x")
    PADDED_UBOOT_SIZE=$(stat --dereference  --printf="%s" "${DEPLOY_DIR_IMAGE}/${UBOOT_BINARY}" | xargs printf "0x%08x")

    mkimage -A arm -T script -C none -n "Flash" -d "${WORKDIR}/uboot.script" "${WORKDIR}/boot.scr"
}

do_deploy() {
    echo "chip-u-boot-scr:do_deploy to " ${DEPLOYDIR}
    install -d ${DEPLOYDIR}
    install -m 0644 ${WORKDIR}/boot.scr ${DEPLOYDIR}/boot.scr-${PV}-${PR}
    ln -sf boot.scr-${PV}-${PR} ${DEPLOYDIR}/boot.scr
    echo ============================================
    ls -l ${DEPLOYDIR}
    echo ============================================

    cat > ${DEPLOYDIR}/flash_CHIP_board.sh-${PV}-${PR} <<-EOF
	#!/bin/bash
	# flash_CHIP_board.sh <image name>
	# will default to environment variable UBI_IMAGE if set.
	FLASH_SPL=false
	FLASH_UBOOT=false
	DEP_DIR_IMAGE=${DEPLOY_DIR_IMAGE}

	if [ ! -n "\${UBI_IMAGE}" ]; then
	    echo "Error: UBI_IMAGE environment variable unset."
	    echo "Please set UBI_IMAGE to the name of the root filesystem image to deploy"
	    exit -1
	elif [ ! -e "\${DEP_DIR_IMAGE}/\${UBI_IMAGE}" ]; then
	    echo "Error: UBI_IMAGE file \"\${DEP_DIR_IMAGE}/\${UBI_IMAGE}\" does not exist."
	    exit -1
	else
	    CURRENT_UBI_SIZE=\$(stat --dereference --printf="%s" \${DEP_DIR_IMAGE}/\${UBI_IMAGE})
	    MAX_UBI_SIZE=\$(printf %d ${UBI_SIZE})
	    if [ \${CURRENT_UBI_SIZE} -gt \${MAX_UBI_SIZE} ]; then
	        echo "Error: UBI_IMAGE file \"\${DEP_DIR_IMAGE}/\${UBI_IMAGE}\" is too large."
	        echo "Current file size is \${CURRENT_UBI_SIZE}"
	        echo "Max file size is ${MAX_UBI_SIZE}"
	        exit -1
	    fi
	fi

	if [ "\${1}" == "--continue" ]; then
	    fastboot ${VID} continue
	    exit 0
	fi

	if [ "\${1}" == "--bootloader" ]; then
	    FLASH_SPL=true
	    FLASH_UBOOT=true

	    # boot the fastboot program loader
	    sunxi-fel uboot \${DEP_DIR_IMAGE}/${SPL_BINARY} write ${SCRIPTADDR} \${DEP_DIR_IMAGE}/boot.scr
	    sleep 8
	fi

	if \$FLASH_SPL; then
	    fastboot ${VID} erase spl
	    fastboot ${VID} erase spl-backup
	    fastboot ${VID} flash spl        \${DEP_DIR_IMAGE}/${SPL_ECC_BINARY}
	    fastboot ${VID} flash spl-backup \${DEP_DIR_IMAGE}/${SPL_ECC_BINARY}
	fi

	if \$FLASH_UBOOT; then
	    fastboot ${VID} erase uboot
	    fastboot ${VID} flash uboot \${DEP_DIR_IMAGE}/${UBOOT_BINARY}
	fi

	fastboot ${VID} erase UBI
	fastboot ${VID} flash UBI \${DEP_DIR_IMAGE}/\${UBI_IMAGE}
	fastboot ${VID} continue -u
	EOF

    chmod +x ${DEPLOYDIR}/flash_CHIP_board.sh-${PV}-${PR}

    ln -sf flash_CHIP_board.sh-${PV}-${PR} ${DEPLOYDIR}/flash_CHIP_board.sh
}

addtask do_deploy after do_compile before do_build

COMPATIBLE_MACHINE = "chippro"
