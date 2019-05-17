if [[ $2 == 'reset' ]]
then
	rm -rf parts completed

    mkdir -p completed/parts
	mkdir parts
    
    echo "Splitting Source file into $1 components..."
    time split -n $1 --numeric-suffixes "../data/ARC-V1-Feb2018-2/ARC_Corpus.txt" parts/part_

    echo "Cleaning Parts according to UTF-8"
    cd parts

    for each in *
    do
    	printf "\r$each"
    	iconv -f utf-8 -t utf-8 -c "$each" > temp 2> /dev/null
    	mv temp "$each"
    done
    cd ..
fi

rm parts/*.openie
rm parts/*.exceptions

echo "Compiling.."

mvn compile
cstatus=$?
echo "Running"

if [ $cstatus -eq 0 ]; then
    mvn exec:exec
else
    echo "Compilation Failed.."
fi