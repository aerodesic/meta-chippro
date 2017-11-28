# Overwrite the fstab with our own
FILESEXTRAPATHS_append := "${THISDIR}/${PN}:"

SRC_URI += " \
	file://fstab \
	"

do_install_append() {
	install -m 644 ${WORKDIR}/fstab ${D}${sysconfdir}
}

