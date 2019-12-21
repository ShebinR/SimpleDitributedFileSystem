server1=""
server2=""
server3=""
if [ "$#" -ne 3 ]; then
    echo "Usage: sh copyServerJar.jar <shebin|sandy> <jar_path> <dst_path>"
    exit
fi

if [[ $1 == "shebin" ]]; then
    echo "SHEBIN"
    server1=$3/server1
    server2=$3/server2
    server3=$3/server3
fi
cp $2 $server1
cp $2 $server2
cp $2 $server3
