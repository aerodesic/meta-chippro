SUMMARY = "Image flasher for chippro project"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
inherit deploy

COMPATIBLE_MACHINE = "chippro"

UBOOT_SCRIPTADDR ?= "0x43100000"

DEPENDS = "u-boot-mkimage-native"

# Flash device vendor ID required for fastboot
VID = "-i 0x1f3a"

do_compile[depends] += "u-boot-chip:do_deploy"

do_deploy() {

    # Create the flashing script
    cat > ${DEPLOYDIR}/flash_CHIP_board.sh-${PV}-${PR} <<-EOF
	#!/bin/bash
	# flash_CHIP_board.sh <image name>
	# will default to environment variable UBI_IMAGE if set.

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
	    # boot the fastboot program loader
	    sunxi-fel uboot ${SPL_BINARY} write ${UBOOT_SCRIPTADDR} boot.scr
	    sleep 8
	fi

	fastboot ${VID} erase spl
	fastboot ${VID} erase spl-backup
	fastboot ${VID} flash spl           ${SPL_ECC_BINARY}
	fastboot ${VID} flash spl-backup    ${SPL_ECC_BINARY}

	fastboot ${VID} erase uboot
	fastboot ${VID} flash uboot         ${UBOOT_BINARY}

	fastboot ${VID} erase UBI
	fastboot ${VID} flash UBI           \${UBI_IMAGE}
	fastboot ${VID} continue -u
	EOF

    chmod +x ${DEPLOYDIR}/flash_CHIP_board.sh-${PV}-${PR}

    ln -sf flash_CHIP_board.sh-${PV}-${PR} ${DEPLOYDIR}/flash_CHIP_board.sh
}


addtask do_deploy before do_build
