(ns data-info.services.exists
  (:require [dire.core :refer [with-pre-hook! with-post-hook!]]
            [clj-jargon.item-info :as item]
            [clj-jargon.permissions :as perm]
            [clojure-commons.file-utils :as ft]
            [data-info.util.irods :as irods]
            [data-info.util.logging :as log]
            [data-info.util.validators :as duv]))

(defn- path-exists-for-user?
  [cm user path]
  (let [path (ft/rm-last-slash path)]
    (and (item/exists? cm path)
         (perm/is-readable? cm user path))))

(defn do-exists
  [{user :user} {paths :paths}]
  (irods/with-jargon-exceptions [cm]
    (duv/user-exists cm user)
    {:paths (into {} (map (juxt keyword (partial path-exists-for-user? cm user)) (set paths)))}))

(with-pre-hook! #'do-exists
  (fn [params body]
    (log/log-call "do-exists" params)
    (duv/validate-num-paths (:paths body))))

(with-post-hook! #'do-exists (log/log-func "do-exists"))
