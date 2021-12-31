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
 (fn-traced
  [_ _]
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
                  (if (s/valid? ::specs/budget-item-name default-name)
                    "Name required"
                    nil))
        (assoc-in [:new-item :amount] default-amount)
        (assoc-in [:new-item :amount-error]
                  (if (s/valid? ::specs/amount default-amount)
                    "0 or greater required"
                    nil))))))

(re-frame/reg-event-db
 ::set-new-item-name
 (fn-traced
  [db [_ name]]
  (assert ::specs/budget-item-name name)
  (assoc-in db [:new-item :name] name)))

(re-frame/reg-event-db
 ::set-new-item-name-error
 (fn-traced
  [db [_ msg]]
  (assert string? msg)
  (assoc-in db [:new-item :name-error] msg)))

(re-frame/reg-event-db
 ::set-new-item-amount
 (fn-traced
  [db [_ amount]]
  (assert ::specs/amount amount)
  (assoc-in db [:new-item :amount] amount)))

(re-frame/reg-event-db
 ::set-new-item-amount-error
 (fn-traced
  [db [_ msg]]
  (assert string? msg)
  (assoc-in db [:new-item :amount-error] msg)))

(re-frame/reg-event-db
 ::add-item
 (fn-traced
  [db [_ _]]
  (let [name (get-in db [:new-item :name])
        amount (get-in db [:new-item :amount])
        item {:budget-item-name name
              :spent {:amount 0 :currency-code "€"}
              :limit {:amount amount :currency-code "€"}}
        budget (:budget db)
        id-map {:budget-item-id (budget/next-item-id budget)}
        item (conj item id-map)]
    (assert ::specs/budget-item item)
    (let [updated (-> db
                      (update-in [:budget] conj item)
                      (update :adding-item not))]
      (if (s/valid? ::specs/budget (:budget updated))
        updated
        (assoc-in db
                  [:new-item :name-error]
                  "Duplicate item name"))))))

(re-frame/reg-event-db
 ::select-item
 (fn-traced
  [db [_ item]]
  (let [item-id (:budget-item-id item)]
    (update db :selected-item-id #(if (= % item-id)
                                    false
                                    item-id)))))

(re-frame/reg-event-db
 ::deselect-all
 (fn-traced
  [db [_ _]]
   (assoc db :selected-item-id nil)))

(re-frame/reg-event-db
 ::spending
 (fn-traced
  [db [_ item]]
  (assoc-in db [:spending :item-id] (:budget-item-id item))))

(re-frame/reg-event-db
 ::cancel-spending
 (fn-traced
  [db [_ _]]
  (assoc db :spending nil)))

(re-frame/reg-event-db
 ::set-spending-amount
 (fn-traced
  [db [_ amount]]
  (assert ::specs/amount amount)
  (assoc-in db [:spending :amount] amount)))

(re-frame/reg-event-db
 ::set-spending-amount-error
 (fn-traced
  [db [_ msg]]
  (assert string? msg)
  (assoc-in db [:spending :amount-error] msg)))

(re-frame/reg-event-db
 ::spend
 (fn-traced
  [db [_ _]]
  (let [item-id (get-in db [:spending :item-id])
        amount (get-in db [:spending :amount])
        item {:budget-item-id item-id}]
    (-> db
        (update :budget budget/spend item amount)
        (assoc :spending nil)))))

(re-frame/reg-event-db
 ::editing
 (fn-traced
  [db [_ item]]
  (assoc-in db [:edit-item :item-id] (:budget-item-id item))))

(re-frame/reg-event-db
 ::cancel-editing
 (fn-traced
  [db [_ _]]
  (assoc db :edit-item nil)))

(re-frame/reg-event-db
 ::set-edit-item-name
 (fn-traced
  [db [_ name]]
  (assert ::specs/budget-item-name name)
  (assoc-in db [:edit-item :name] name)))

(re-frame/reg-event-db
 ::set-edit-item-name-error
 (fn-traced
  [db [_ msg]]
  (assert string? msg)
  (assoc-in db [:edit-item :name-error] msg)))

(re-frame/reg-event-db
 ::set-edit-item-amount
 (fn-traced
  [db [_ amount]]
  (assert ::specs/amount amount)
  (assoc-in db [:edit-item :amount] amount)))

(re-frame/reg-event-db
 ::set-edit-item-amount-error
 (fn-traced
  [db [_ msg]]
  (assert string? msg)
  (assoc-in db [:edit-item :amount-error] msg)))

(re-frame/reg-event-db
 ::edit
 (fn-traced
  [db [_ _]]
  (let [item-id (get-in db [:edit-item :item-id])
        name (get-in db [:edit-item :name])
        amount (get-in db [:edit-item :amount])
        item {:budget-item-id item-id}
        updated
        (-> db
            (update :budget budget/set-item-name item name)
            (update :budget budget/set-limit-amount item amount)
            (assoc :edit-item nil))]
    (if (s/valid? ::specs/budget updated)
      updated
      (assoc-in db [:edit-item :name-error] "Duplicate item name")))))

(re-frame/reg-event-db
 ::toggle-resetting-all
 (fn-traced
  [db [_ _]]
  (update db :resetting-all not)))

(re-frame/reg-event-db
 ::reset-all-items
 (fn-traced
  [db [_ _]]
  (let [updated (update db :budget budget/reset-all-items)]
    (if (s/valid? ::specs/budget (:budget db))
      (update updated :resetting-all not)
      db))))

(re-frame/reg-event-db
 ::reset-item
 (fn-traced
  [db [_ item]]
  (update db :budget budget/reset-item item)))

(re-frame/reg-event-fx
  ::navigate
  (fn-traced
   [_ [_ handler]]
   {:navigate handler}))

(re-frame/reg-event-fx
 ::set-active-panel
 (fn-traced
  [{:keys [db]} [_ active-panel]]
  {:db (assoc db :active-panel active-panel)}))
