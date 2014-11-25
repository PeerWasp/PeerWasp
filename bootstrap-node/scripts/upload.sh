#!/bin/bash

function upload_package {
    echo "upload package to $1"
    scp bootstrap.tar.gz uzh_peerbox@$1:~/peerbox/.
}

function extract_package {
    echo "extract package on $1"
    ssh -i ~/.ssh/emanicslab.org uzh_peerbox@$1 'cd peerbox; tar xzf bootstrap.tar.gz'
}

upload_package $1
extract_package $1