package main

import (
	"log"
	"os"
)

//    "data/Recreational_Trails.kml"

// ReadKmlFile - file for reading
func ReadKmlfile(fileName string) {
	kmlFile, err := os.Open(fileName)
	if err != nil {
		log.Printf("Error opening:%s", fileName)
	}

	defer kmlFile.Close()
	log.Printf("opened file:%s", fileName)

}
