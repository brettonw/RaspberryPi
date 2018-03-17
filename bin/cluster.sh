declare -a CLUSTER_NAMES=("rpi-c0.local" "rpi-c1.local" "rpi-c2.local" "rpi-c3.local");
CLUSTER_SIZE=${#CLUSTER_NAMES[@]}
for (( i=0; i<${CLUSTER_SIZE}; i++ )); do
    MACHINE_NAME=${CLUSTER_NAMES[$i]};
    MACHINE_ADDRESS="$(ping $MACHINE_NAME -c1 | head -1 | grep -Eo '[0-9.]{4,}')";
    # echo "$MACHINE_NAME: $MACHINE_ADDRESS";
    ssh $MACHINE_NAME "$*";
done
