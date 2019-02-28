(defproject lobster-writer "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.520"]
                 [reagent "0.8.1"]
                 [re-frame "0.10.5"]
                 [secretary "1.2.3"]
                 [garden "1.3.5"]
                 [ns-tracker "0.3.1"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-garden "0.2.8"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"
                                    "resources/public/css"]

  :figwheel {:css-dirs ["resources/public/css"]
             :nrepl-port 7888}

  :garden {:builds [{:id "screen"
                     :source-paths ["src/clj"]
                     :stylesheet lobster-writer.css/screen
                     :compiler {:output-to "resources/public/css/screen.css"
                                :pretty-print? true}}]}

  :profiles
  {:dev
   {:dependencies [[day8.re-frame/re-frame-10x "0.3.3"]
                   [day8.re-frame/tracing "0.5.1"]
                   [figwheel-sidecar "0.5.18"]]
    :plugins [[lein-doo "0.1.8"]
              [lein-figwheel "0.5.18"]]}
   :prod {:dependencies [[day8.re-frame/tracing-stubs "0.5.1"]]}}

  :cljsbuild
  {:builds
   [{:id "dev"
     :source-paths ["src/cljs"]
     :figwheel {:on-jsload "lobster-writer.core/mount-root"}
     :compiler {:main lobster-writer.core
                :output-to "resources/public/js/compiled/app.js"
                :output-dir "resources/public/js/compiled/out"
                :asset-path "js/compiled/out"
                :source-map-timestamp true
                :closure-defines {"re_frame.trace.trace_enabled_QMARK_" true
                                  "day8.re_frame.tracing.trace_enabled_QMARK_" true}
                :external-config {:devtools/config {:features-to-install :all}}
                :install-deps true
                :npm-deps {react-quill "1.3.3"
                           react "16.8.1"
                           react-dom "16.8.1"}
                :process-shim true
                :infer-externs true}}

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
  )
