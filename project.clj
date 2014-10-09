(defproject jaob "0.1.0-SNAPSHOT"
  :description "This lib allows you to map Java Objects to OWL (it is similar to JAXB). Original project: https://code.google.com/p/jaob/"
  :url "https://github.com/tranchis/jaob"
  :repositories [["catbull" "http://carmen.ncl.ac.uk/maven/repo/"]]
  :dependencies [[owlapi/api "2.2.0"]
                 [owlapi/apibinding "2.2.0"]
                 [junit/junit "4.11"]
                 [com.sun.codemodel/codemodel "2.6"]
                 [org.slf4j/slf4j-log4j12 "1.7.6"]]
  :java-source-paths ["java"]
  :test-paths ["test"])
