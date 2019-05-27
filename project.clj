(defproject lobster-writer "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.520"]
                 [reagent "0.8.0"]
                 [re-frame "0.10.5"]
                 [bidi "2.1.5"]
                 [kibu/pushy "0.3.8"]
                 [garden "1.3.5"]
                 [ns-tracker "0.3.1"]
                 [re-com "2.4.0"]
                 [cljsjs/react-quill "1.1.0-0"]
                 [cljsjs/prop-types "15.6.2-0"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [cljsjs/filesaverjs "1.3.3-0"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-garden "0.3.0"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/cljs"]


  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"]

  :figwheel {:css-dirs ["resources/public/css"]
             :nrepl-port 7888
             :nrepl-middleware [cider.piggieback/wrap-cljs-repl]
             :ring-handler figwheel-server/handler}

  :garden {:builds [{:id "screen"
                     :source-paths ["src/clj"]
                     :stylesheet lobster-writer.css/screen
                     :compiler {:output-to "resources/public/css/screen.css"
                                :pretty-print? true}}]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.10"]
                   [day8.re-frame/re-frame-10x "0.3.3"]
                   [day8.re-frame/tracing "0.5.1"]
                   [figwheel-sidecar "0.5.18"]
                   [cider/piggieback "0.4.0"]]
    :plugins [[lein-doo "0.1.8"]]
    :source-paths ["dev"]}
   :prod {:dependencies [[day8.re-frame/tracing-stubs "0.5.1"]]}}

  :cljsbuild
  {:builds
   [{:id "dev"
     :source-paths ["src/cljs"]
     :figwheel {:on-jsload "lobster-writer.core/mount-root"}
     :compiler {:main lobster-writer.core
                :output-to "resources/public/js/compiled/app.js"
                :output-dir "resources/public/js/compiled/out"
                :asset-path "/js/compiled/out"
                :source-map-timestamp true
                :preloads [devtools.preload
                           day8.re-frame-10x.preload]
                :closure-defines {"re_frame.trace.trace_enabled_QMARK_" true
                                  "day8.re_frame.tracing.trace_enabled_QMARK_" true}
                :external-config {:devtools/config {:features-to-install :all}}
                :process-shim true}}

    {:id "min"
     :source-paths ["src/cljs"]
     :compiler {:main lobster-writer.core
                :output-to "resources/public/js/compiled/app.js"
                :optimizations :advanced
                :closure-defines {goog.DEBUG false}
                :pretty-print false}}

    {:id "test"
     :source-paths ["src/cljs" "test/cljs"]
     :compiler {:main lobster-writer.runner
                :output-to "resources/public/js/compiled/test.js"
                :output-dir "resources/public/js/compiled/test/out"
                :optimizations :none}}]}


  :repositories {"local" "file:lib"}
  )
