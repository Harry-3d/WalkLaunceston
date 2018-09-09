package main

import (
	"fmt"
	"html/template"
	"log"
	"net/http"
)

// index.html
func index(w http.ResponseWriter, r *http.Request) {
	log.Printf("Page:%-15s Method:%s", "index.html", r.Method)

	t, err := template.ParseFiles("web/index.html")
	if err != nil {
		log.Fatal("Template Parse Error:", err)
	} else {
		t.Execute(w, nil)
	}
}

type Page struct {
	TestCount  int
	TestOutput []string
}

var testPg Page

// test.html
func test(w http.ResponseWriter, r *http.Request) {
	log.Printf("Page:%-15s Method:%s", "test.html", r.Method)

	if r.Method == "POST" {
		r.ParseForm()
		fmt.Println("Test recv:", r.Form["comms"])
		testPg.TestOutput = r.Form["comms"]
		testPg.TestCount++

	}

	// Show page
	t, err := template.ParseFiles("web/test.html")
	if err != nil {
		log.Fatal("Template Parse Error:", err)
	} else {
		t.Execute(w, &testPg)
	}

}

func main() {
	//ReadKmlfile("data/Recreational_Trails.kml")
	initSvr()

}

// initSver - Start HTTP server.
func initSvr() {
	log.Println("Init Server.")

	// Link paths to functions
	// When user hits / run test()
	http.HandleFunc("/test", test)
	http.HandleFunc("/", index)

	// Serve static files
	http.Handle("/web/", http.StripPrefix("/web/", http.FileServer(http.Dir("web"))))
	http.Handle("/data/", http.StripPrefix("/data/", http.FileServer(http.Dir("data"))))

	err := http.ListenAndServe("192.168.1.112:8888", nil)
	if err != nil {
		log.Fatal("ListenAndServe:", err)
	}
}
