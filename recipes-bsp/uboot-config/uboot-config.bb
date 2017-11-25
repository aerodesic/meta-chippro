SUMMARY = "U-boot boot scripts for CHIP boards"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

inherit deploy

UBOOT_SCRIPTADDR ?= "0x43100000"

DEPENDS = "u-boot-mkimage-native"

# Flass device vendor ID required for fastboot
VID = "-i 0x1f3a"

SRC_URI = " \
	file://uboot.script \
	file://uboot-env \
        "

S = "${WORKDIR}"

do_compile[depends] += "u-boot-chip:do_deploy"

do_compile() {
#    install -m 644 ${B}/${SPL_ECC_BINARY} ${DEPLOYDIR}/${SPL_ECC_BINARY}-${PV}-${PR}
#    ln -sf ${SPL_ECC_BINARY}-${PV}-${PR} ${DEPLOYDIR}/${SPL_ECC_BINARY}
#    install -m 644 ${B}/${SPL_BINARY} ${DEPLOYDIR}/${SPL_BINARY}

    # Create the uboot script
    install -m 755 -d ${DEPLOYDIR}
    mkimage -A arm -T script -C none -n "Flash" -d "${WORKDIR}/uboot.script" "${DEPLOYDIR}/boot.scr"

    # Create uboot-env file
    mkenvimage -s ${ENV_IMAGE_SIZE} -o ${DEPLOYDIR}/uboot-env.bin ${WORKDIR}/uboot-env
}

# Removed from below
#	else
#	    CURRENT_UBI_SIZE=\$(stat --dereference --printf="%s" \${UBI_IMAGE})
#	    MAX_SIZE=\$(printf %d ${MAX_UBI_SIZE})
#	    if [ \${CURRENT_UBI_SIZE} -gt \${MAX_SIZE} ]; then
#	        echo "Error: UBI_IMAGE file \"\${UBI_IMAGE}\" is too large."
#	        echo "Current file size is \${CURRENT_UBI_SIZE}"
#	        echo "Max file size is \${MAX_SIZE}"
#	        exit -1
#	    fi

do_deploy() {
    # Create the flashing script
    cat > ${DEPLOYDIR}/flash_CHIP_board.sh-${PV}-${PR} <<-EOF
	#!/bin/bash
	# flash_CHIP_board.sh <image name>
	# will default to environment variable UBI_IMAGE if set.
	FLASH_SPL=false
	FLASH_UBOOT=false

	if [ "\${1}" == "--continue" ]; then
	    fastboot ${VID} continue
	    exit 0
	fi

	UBI_IMAGE=\${1}
	shift

	if [ -z "\${UBI_IMAGE}" ]; then
	    echo "Error: missing UBI image name."
	    exit -1
	elif [ ! -e "\${UBI_IMAGE}" ]; then
	    echo "Error: UBI_IMAGE file \"\${UBI_IMAGE}\" does not exist."
	    exit -1
	fi

	if [ "\${1}" == "--bootloader" ]; then
	    FLASH_SPL=true
	    FLASH_UBOOT=true

	    # boot the fastboot program loader
	    sunxi-fel uboot ${SPL_BINARY} write ${UBOOT_SCRIPTADDR} boot.scr
	    sleep 8
	fi

	if \$FLASH_SPL; then
	    fastboot ${VID} erase spl
	    fastboot ${VID} erase spl-backup
	    fastboot ${VID} flash spl        ${SPL_ECC_BINARY}
	    fastboot ${VID} flash spl-backup ${SPL_ECC_BINARY}
	fi

	if \$FLASH_UBOOT; then
	    fastboot ${VID} erase uboot
	    fastboot ${VID} flash uboot ${UBOOT_BINARY}
	fi

	fastboot ${VID} erase UBI
	fastboot ${VID} flash UBI \${UBI_IMAGE}
	fastboot ${VID} continue -u
	EOF

    chmod +x ${DEPLOYDIR}/flash_CHIP_board.sh-${PV}-${PR}

    ln -sf flash_CHIP_board.sh-${PV}-${PR} ${DEPLOYDIR}/flash_CHIP_board.sh
}


addtask do_deploy after do_compile before do_build

COMPATIBLE_MACHINE = "chippro"
