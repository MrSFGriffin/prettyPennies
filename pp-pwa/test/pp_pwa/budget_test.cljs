(ns pp-pwa.budget-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [pp-pwa.specs :as specs]
            [pp-pwa.budget :as b]))

(deftest next-item-id-test
  (testing "returns 1 with nil budget"
    (is (= 1 (b/next-item-id nil))))
  (testing "returns 1 with empty budget"
    (is (= 1 (b/next-item-id {})))))
