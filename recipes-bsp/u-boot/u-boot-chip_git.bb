require recipes-bsp/u-boot/u-boot.inc

DESCRIPTION = "U-Boot port for C.H.I.P. boards"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://Licenses/README;md5=a2c678cfd4a4d97135585cad908541c6"

DEPENDS += "dtc-native swig-native python-native coreutils-native u-boot-mkimage-native"

COMPATIBLE_MACHINE = "chippro"

PROVIDES += "u-boot"
RDEPENDS_${PN}_append_chippro = " image-flasher"


PV = "git${SRCPV}"

SRCREV ?= "65972a0b6204aa298b70b7ebd755bb1ce1ed53ee"
SRC_URI = "git://github.com/u-boot/u-boot.git \
           file://CHIP_pro_defconfig \
           file://uboot.script \
           file://0001-Added-host-path-to-libfdt-build.patch \
           file://0002-Add-booting-from-UBI-volume-zImages.patch \
           "


S = "${WORKDIR}/git"

do_configure_prepend() {
    cp ${WORKDIR}/CHIP_pro_defconfig ${S}/configs/
}

do_compile_prepend() {
    install -d -m 755 ${DEPLOYDIR}
    mkimage -A arm -T script -C none -n "Flash" -d "${WORKDIR}/uboot.script" "${DEPLOYDIR}/boot.scr"
}

do_compile_append() {
    # Create padded version of u-boot-dtb.bin
    # dd if=${B}/u-boot.bin of=${B}/u-boot-sunxi-padded.bin bs=${NAND_ERASE_BLOCK_SIZE} conv=sync
    ${OBJCOPY} -I binary -O binary --pad-to=0xc0000 --gap-fill=0 ${B}/u-boot-dtb.bin ${B}/${UBOOT_BINARY}

    install ${B}/spl/${SPL_ECC_BINARY} ${B}/${SPL_ECC_BINARY}
    # Move the file if necessary
    if [ -e ${B}/spl/${SPL_BINARY} ] ; then
      install ${B}/spl/${SPL_BINARY} ${B}/${SPL_BINARY}
    fi
}

#
# Install some things left out of base module
#
do_deploy_append() {
    install -m 644 ${B}/${SPL_ECC_BINARY} ${DEPLOYDIR}/${SPL_ECC_BINARY}-${PV}-${PR}
    ln -sf ${SPL_ECC_BINARY}-${PV}-${PR} ${DEPLOYDIR}/${SPL_ECC_BINARY}

    # Extract environment from u-boot compile
    ${OBJCOPY} -O binary -j ".rodata.default_environment" ${B}/env/common.o ${B}/rawenv.bin
    # Convert NUL bytes to newline
    tr "\0" "\n" <${B}/rawenv.bin >${B}/rawenv.txt
    mkenvimage -s ${ENV_IMAGE_SIZE} -o ${DEPLOYDIR}/${UBOOT_ENV_NAME}-${PV}-${PR} ${B}/rawenv.txt
    ln -sf ${UBOOT_ENV_NAME}-${PV}-${PR} ${DEPLOYDIR}/${UBOOT_ENV_NAME}
    # Remove before flight
    # rm ${B}/rawenv.bin ${B}/rawenv.txt

    install -m 644 ${B}/${UBOOT_BINARY} ${DEPLOYDIR}/${UBOOT_BINARY}-${PV}-${PR}
    ln -sf ${UBOOT_BINARY}-${PV}-${PR} ${DEPLOYDIR}/${UBOOT_BINARY}
}

