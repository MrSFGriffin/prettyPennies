(ns pp-pwa.events
  (:require
   [cljs.spec.alpha :as s]
   [re-frame.core :as re-frame]
   [pp-pwa.budget :as budget]
   [pp-pwa.db :as db]
   [pp-pwa.specs :as specs]
   [day8.re-frame.tracing :refer-macros [fn-traced]]))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
            db/default-db))

(re-frame/reg-event-db
 ::toggle-adding-item
 (fn-traced
  [db _]
  (let [default-name ""
        default-amount 0]
    (-> db
        (update :adding-item not)
        (assoc-in [:new-item :name] default-name)
        (assoc-in [:new-item :name-error]
                  (s/valid? ::specs/budget-item-name default-name))
        (assoc-in [:new-item :amount] default-amount)
        (assoc-in [:new-item :amount-error]
                  (s/valid? ::specs/amount default-amount))))))

(re-frame/reg-event-db
 ::add-item
 (fn-traced [db [_ _]]
            (let [name (get-in db [:new-item :name])
                  amount (get-in db [:new-item :amount])
                  item {:budget-item-name name
                        :spent {:amount 0 :currency-code "€"}
                        :limit {:amount amount :currency-code "€"}}
                  budget (:budget db)
                  id-map {:budget-item-id (budget/next-item-id budget)}
                  item (conj item id-map)]
              (assert ::specs/budget-item item)
              (-> db
                  (update-in [:budget] conj item)
                  (update :adding-item not)))))

(re-frame/reg-event-db
 ::set-new-item-name
 (fn-traced [db [_ name]]
            (assert ::specs/budget-item-name name)
            (assoc-in db [:new-item :name] name)))

(re-frame/reg-event-db
 ::set-new-item-name-error
 (fn-traced [db [_ msg]]
            (assert string? msg)
            (assoc-in db [:new-item :name-error] msg)))

(re-frame/reg-event-db
 ::set-new-item-amount
 (fn-traced [db [_ amount]]
            (assert ::specs/amount amount)
            (assoc-in db [:new-item :amount] amount)))

(re-frame/reg-event-db
 ::set-new-item-amount-error
 (fn-traced [db [_ msg]]
            (assert string? msg)
            (assoc-in db [:new-item :amount-error] msg)))

(re-frame/reg-event-fx
  ::navigate
  (fn-traced [_ [_ handler]]
             {:navigate handler}))

(re-frame/reg-event-fx
 ::set-active-panel
 (fn-traced [{:keys [db]} [_ active-panel]]
            {:db (assoc db :active-panel active-panel)}))
