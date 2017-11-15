require recipes-bsp/u-boot/u-boot.inc

DESCRIPTION = "U-Boot port for C.H.I.P. boards"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://Licenses/README;md5=a2c678cfd4a4d97135585cad908541c6"

DEPENDS += "dtc-native swig-native python-native coreutils-native"
PROVIDES += "u-boot"

EXTRA_OEMAKE+="KBUILD_VERBOSE='1' CFLAGS='${CFLAGS} -v'"

PV = "git${SRCPV}"

SRCREV ?= "57270eca55b4e72b4af6c78ac066466dba7c6d70"
SRC_URI = " \
	git://github.com/u-boot/u-boot.git \
        file://0001-Added-host-path-to-libfdt-swig-build.patch \
        "

S = "${WORKDIR}/git"

do_compile_append() {
    install ${B}/spl/${SPL_ECC_BINARY} ${B}/${SPL_ECC_BINARY}
}

COMPATIBLE_MACHINE = "chippro"

do_deploy_append() {
    install -m 644 ${B}/${SPL_ECC_BINARY} ${DEPLOYDIR}/${SPL_ECC_BINARY}-${PV}-${PR}
    ln -sf ${SPL_ECC_BINARY}-${PV}-${PR} ${DEPLOYDIR}/${SPL_ECC_BINARY}
    install -m 644 ${B}/${SPL_BINARY} ${DEPLOYDIR}/${SPL_BINARY}

    # Create the flashing script
    cat > ${DEPLOYDIR}/flash_CHIP_board.sh-${PV}-${PR} <<-EOF
	#!/bin/bash
	# flash_CHIP_board.sh <image name>
	# will default to environment variable UBI_IMAGE if set.
	FLASH_SPL=false
	FLASH_UBOOT=false
	UBI_IMAGE=${IMAGE_BASENAME}-${MACHINE}.ubi.sparse

	if [ ! -e "\${UBI_IMAGE}" ]; then
	    echo "Error: UBI_IMAGE file \"\${UBI_IMAGE}\" does not exist."
	    exit -1
	else
	    CURRENT_UBI_SIZE=\$(stat --dereference --printf="%s" \${UBI_IMAGE})
	    MAX_SIZE=\$(printf %d ${MAX_UBI_SIZE})
	    if [ \${CURRENT_UBI_SIZE} -gt \${MAX_SIZE} ]; then
	        echo "Error: UBI_IMAGE file \"\${UBI_IMAGE}\" is too large."
	        echo "Current file size is \${CURRENT_UBI_SIZE}"
	        echo "Max file size is \${MAX_SIZE}"
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
	    sunxi-fel uboot ${SPL_BINARY} write ${SCRIPTADDR} boot.scr
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
