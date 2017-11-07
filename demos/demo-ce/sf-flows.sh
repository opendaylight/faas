#!/usr/bin/env bash

set -e
hostnum=${HOSTNAME#"gbpsfc"}
sw="sw$hostnum"
nsp=$1

if [ "$hostnum" -eq "3" ]; then
    TUNNEL=0xC0A83247
elif [ "$hostnum" -eq "5" ]; then
    TUNNEL=0xC0A83249
else
    echo "Invalid SF for this demo";
    exit
fi
# delete NORMAL, if present
nsphex=`printf "%x\n" $nsp`
sudo ovs-ofctl --strict del-flows $sw priority=0
sudo ovs-ofctl add-flow $sw "priority=1000,nsi=255 actions=move:NXM_NX_NSH_MDTYPE[]->NXM_NX_NSH_MDTYPE[],move:NXM_NX_NSH_NP[]->NXM_NX_NSH_NP[],move:NXM_NX_NSP[]->NXM_NX_NSP[],load:254->NXM_NX_NSI[],move:NXM_NX_NSH_C1[]->NXM_NX_NSH_C1[],move:NXM_NX_NSH_C2[]->NXM_NX_NSH_C2[],move:NXM_NX_TUN_ID[0..31]->NXM_NX_TUN_ID[0..31],load:$TUNNEL->NXM_NX_TUN_IPV4_DST[],load:0x4->NXM_NX_TUN_GPE_NP[],IN_PORT" -OOpenFlow13
sudo ovs-ofctl add-flow $sw "priority=1000,nsi=254 actions=move:NXM_NX_NSH_MDTYPE[]->NXM_NX_NSH_MDTYPE[],move:NXM_NX_NSH_NP[]->NXM_NX_NSH_NP[],move:NXM_NX_NSP[]->NXM_NX_NSP[],load:253->NXM_NX_NSI[],move:NXM_NX_NSH_C1[]->NXM_NX_NSH_C1[],move:NXM_NX_NSH_C2[]->NXM_NX_NSH_C2[],move:NXM_NX_TUN_ID[0..31]->NXM_NX_TUN_ID[0..31],load:$TUNNEL->NXM_NX_TUN_IPV4_DST[],load:0x4->NXM_NX_TUN_GPE_NP[],IN_PORT" -OOpenFlow13
