SUMMARY = "Image flasher for chippro project"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
inherit deploy

COMPATIBLE_MACHINE = "chippro"

UBOOT_SCRIP_TADDR ?= "0x43100000"

# DEPENDS = "u-boot-mkimage-native"

# Flash device vendor ID required for fastboot
# VID = "-i 0x1f3a -n ${NAND_PAGE_SIZE}"
VID = "-i 0x1f3a"

do_compile[depends] += "u-boot-chip:do_deploy"

do_deploy() {

    # Create the flashing script
    cat > ${DEPLOYDIR}/flash_board.sh <<-EOF
	#!/bin/bash
	UBI_IMAGE=\${1}

	if [ -z "\${UBI_IMAGE}" ]; then
	    echo "Error: missing UBI image name."
	    exit -1
	elif [ ! -e "\${UBI_IMAGE}" ]; then
	    echo "Error: UBI_IMAGE file \"\${UBI_IMAGE}\" does not exist."
	    exit -1
	fi

	echo Starting SPL
	sunxi-fel spl sunxi-spl.bin
	sleep 1

	echo Writing ${UBOOT_INSTALL_FILE} to ram
	sunxi-fel write ${UBOOT_MEM_ADDR} ${UBOOT_INSTALL_FILE}

	echo Writing ${SPL_INSTALL_FILE} to ram
	sunxi-fel write ${SPL_MEM_ADDR} ${SPL_INSTALL_FILE}

	echo Writing uboot commands ram
	sunxi-fel write ${UBOOT_SCRIPT_ADDR} ${UBOOT_SCRIPT_BASE_NAME}.bin

	echo Starting boot
	sunxi-fel exe ${UBOOT_MEM_ADDR}

	fastboot ${VID} erase UBI
	fastboot ${VID} flash UBI           \${UBI_IMAGE}

	fastboot ${VID} continue -u

	EOF

    chmod +x ${DEPLOYDIR}/flash_board.sh
}


addtask do_deploy before do_build
