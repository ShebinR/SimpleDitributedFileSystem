ps aux | grep "[j]ava -jar" | grep $1 | awk '{print $2}'
