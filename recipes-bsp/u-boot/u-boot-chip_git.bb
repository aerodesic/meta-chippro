require recipes-bsp/u-boot/u-boot.inc

DESCRIPTION = "U-Boot port for C.H.I.P. boards"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://Licenses/README;md5=a2c678cfd4a4d97135585cad908541c6"

DEPENDS += "dtc-native swig-native python-native coreutils-native u-boot-mkimage-native"

COMPATIBLE_MACHINE = "chippro"

PROVIDES += "u-boot"
RDEPENDS_${PN}_append_chippro = " image-flasher"


PV = "git${SRCPV}"

# SRCREV ?= "0c4d24823ed28c94dae56a10a66687c69b70c1d1"
# Same version as u-boot-mkimage-native
SRCREV = "0c4d24823ed28c94dae56a10a66687c69b70c1d1"

SRC_URI = " \
	git://github.com/u-boot/u-boot.git \
	file://CHIP_pro_defconfig \
	file://uboot.script \
	file://0001-Added-host-path-to-libfdt-build.patch \
	file://0002-Add-booting-from-UBI-volume-zImages.patch \
	file://0003-Change-mutex_is_locked-to-return-TRUE-rather-than-FA.patch \
    "


S = "${WORKDIR}/git"

do_compile_prepend() {
    # Install the local copy of the defconfig
    install ${WORKDIR}/CHIP_pro_defconfig ${S}/configs/
}
 
# Install some things left out of base module
do_deploy_append() {

    # Extract environment from u-boot compile
    ${OBJCOPY} -O binary -j ".rodata.default_environment" ${B}/env/common.o ${B}/rawenv.bin

    # Convert NUL bytes to newline
    tr "\0" "\n" <${B}/rawenv.bin >${B}/rawenv.txt

    mkenvimage -s ${ENV_IMAGE_SIZE} -o ${DEPLOYDIR}/${UBOOT_ENV_NAME} ${B}/rawenv.txt

    # Strip out the mtd information and add it to the boot text script
    grep "^mtd" ${B}/rawenv.txt >${B}/boot.txt
    cat ${WORKDIR}/uboot.script >>${B}/boot.txt

    install ${B}/spl/${SPL_ECC_BINARY} ${DEPLOYDIR}
    install ${B}/spl/sunxi-spl.bin ${DEPLOYDIR}
    install ${B}/${SPL_BINARY} ${DEPLOYDIR}

    mkimage -A arm -T script -C none -n "Flash" -d "${B}/boot.txt" "${DEPLOYDIR}/boot.scr"

    # Remove before flight
    # rm ${B}/rawenv.bin ${B}/rawenv.txt ${B}/boot.txt
}

