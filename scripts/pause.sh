val=`ps aux | grep "[j]ava -jar" | grep $1 | awk '{print $2}'`
echo "Pausing $1 : PID -> $val"
kill -STOP $val
