val=`ps aux | grep "[j]ava -jar" | grep $1 | awk '{print $2}'`
echo "Resuming $1 : PID -> $val"
kill -CONT $val
