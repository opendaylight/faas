echo "SW1 and SW6's vxlan port is created by faas, so delete the original vxlan port created by gbpsfc"
vagrant ssh gbpsfc1 -c "sudo ovs-vsctl del-port sw1 sw1-vxlan-0"
vagrant ssh gbpsfc1 -c "sudo ovs-vsctl del-port sw1 sw1-vxlangpe-0"
vagrant ssh gbpsfc6 -c "sudo ovs-vsctl del-port sw6 sw6-vxlan-0"
vagrant ssh gbpsfc6 -c "sudo ovs-vsctl del-port sw6 sw6-vxlangpe-0"