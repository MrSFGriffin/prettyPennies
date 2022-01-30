(ns pp-pwa.events
  (:require
   [cljs.spec.alpha :as s]
   [expound.alpha :as ex]
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [pp-pwa.budget :as budget]
   [pp-pwa.db :as db]
   [pp-pwa.specs :as specs]
   [pp-pwa.storage :as storage]
   [day8.re-frame.tracing :refer-macros [fn-traced]]))

(defn scroll-to-id
  [id]
  (-> js/document
      (.getElementById id)
      (.scrollIntoView true))
  (-> js/document
      (.body)
      (.scrollTo 0 0)))

(defn goto-selected
  [db]
  (let [item-id (:selected-item-id db)]
    (when item-id
      (reagent/after-render #(scroll-to-id (str "budget-item-" item-id))))))

(defn focus
  [id]
  (-> js/document
      (.getElementById id)
      (.focus)))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced
  [_ _]
  db/default-db))

(re-frame/reg-event-db
 ::set-budget-data-view
 (fn-traced
  [db [_ view]]
  (assoc db :budget-data-view view)))

(re-frame/reg-event-db
 ::toggle-loading
 (fn-traced
  [db [_ _]]
  (update db :loading not)))

(re-frame/reg-event-db
 ::set-budget
 (fn-traced
  [db [_ budget]]
  (assoc db :budget budget)))

(re-frame/reg-event-db
 ::set-plan-income
 (fn-traced
  [db [_ income]]
  (assoc-in db [:plan :income] income)))

(re-frame/reg-event-db
 ::set-plan-items
 (fn-traced
  [db [_ items]]
  (assoc-in db [:plan :budget] items)))

(re-frame/reg-event-db
 ::toggle-adding-item
 (fn-traced
  [db _]
  (when (:adding-item db)
    (goto-selected db))
  (let [default-name ""
        default-amount 0]
    (when (not (:adding-item db)) ; scroll to the add item controls
      (reagent/after-render
       #(do
          (scroll-to-id "item-name-input")
          (focus "item-name-input"))))
    (-> db
        (assoc :resetting-all nil)
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
  [db [_ view-mode]]
  (goto-selected db)
  (let [name (get-in db [:new-item :name])
        amount (get-in db [:new-item :amount])
        item {:budget-item-name name
              :spent {:amount 0 :currency-code "€"}
              :limit {:amount amount :currency-code "€"}}
        keys (cond
               (= view-mode :budget) [:budget]
               (= view-mode :plan) [:plan :budget])
        save-fn (cond
                  (= view-mode :budget) storage/save-budget-item
                  (= view-mode :plan) storage/save-plan-item)
        budget (get-in db keys)
        id-map {:budget-item-id (budget/next-item-id budget)}
        item (conj item id-map)]
    (assert ::specs/budget-item item)
    (let [updated (-> db
                      (update-in keys conj item)
                      (update :adding-item not))]
      (if (s/valid? ::specs/budget (get-in updated keys))
        (do
          (save-fn item #(js/console.log "saved item"))
          updated)
        (assoc-in db
                  [:new-item :name-error]
                  "Duplicate item name"))))))

(re-frame/reg-event-db
 ::select-item
 (fn-traced
  [db [_ item]]
  (let [item-id (:budget-item-id item)]
    (reagent/after-render #(scroll-to-id (str "budget-item-" item-id)))
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
  (reagent/after-render
   #(do (scroll-to-id (str "budget-item-" (:budget-item-id item)))
        (focus "spend-amount-input")))
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
        item {:budget-item-id item-id}
        updated (-> db
                    (update :budget budget/spend item amount)
                    (assoc :spending nil))
        item (->> updated
                  :budget
                  (filter #(= item-id (:budget-item-id %)))
                  first)]
    (storage/save-budget-item item #(js/console.log "Item updated."))
      updated)))

(re-frame/reg-event-db
 ::editing
 (fn-traced
  [db [_ item]]
  (let [updated (assoc-in db [:edit-item :item-id] (:budget-item-id item))
        updated (assoc-in updated [:edit-item :name] (:budget-item-name item))
        updated (assoc-in updated [:edit-item :amount] (-> item :limit :amount))]
    updated)))

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
  [db [_ _ view-mode]]
  (let [item-id (get-in db [:edit-item :item-id])
        name (get-in db [:edit-item :name])
        amt (get-in db [:edit-item :amount])
        keys (cond
               (= view-mode :budget) [:budget]
               (= view-mode :plan) [:plan :budget])
        save-fn (cond
                  (= view-mode :budget) storage/save-budget-item
                  (= view-mode :plan) storage/save-plan-item)
        item (budget/get-item (get-in db keys) item-id)
        updated (-> db
                    (update-in keys budget/set-item-name item name)
                    (update-in keys budget/set-limit-amount item amt)
                    (assoc :edit-item nil))
        item (budget/get-item (get-in updated keys) item-id)]
    (if (s/valid? ::specs/budget (:budget updated))
      (do
        (save-fn item #(js/console.log "Item updated."))
        updated)
      (assoc-in db [:edit-item :name-error]
                (ex/expound ::specs/budget (:budget updated)))))))

(re-frame/reg-event-db
 ::deleting
 (fn-traced
 [db [_ item]]
 (assoc-in db [:delete-item :item-id] (:budget-item-id item))))

 (re-frame/reg-event-db
  ::cancel-deleting
  (fn-traced
   [db [_ item]]
   (assoc db :delete-item nil)))

(re-frame/reg-event-db
 ::delete
 (fn-traced
  [db [_ item-id view-mode]]
  (let [item {:budget-item-id item-id :id item-id}
        keys (cond
               (= view-mode :budget) [:budget]
               (= view-mode :plan) [:plan :budget])
        budget (get-in db keys)
        budget (vec (remove #(= item-id (:budget-item-id %)) budget))
        updated (assoc-in db keys budget)]
    (storage/delete-budget-item item #(js/console.log "Deleted item."))
    updated)))

(re-frame/reg-event-db
 ::toggle-resetting-all
 (fn-traced
  [db [_ _]]
  (when (:resetting-all db)
    (goto-selected db))
  (-> db
      (update :resetting-all not)
      (assoc :adding-item nil))))

(re-frame/reg-event-db
 ::reset-all-items
 (fn-traced
  [db [_ _]]
  (goto-selected db)
  (let [updated (update db :budget budget/reset-all-items)]
    (if (s/valid? ::specs/budget (:budget db))
      (let [updated (update updated :resetting-all not)
            budget (:budget updated)
            msg-fn #(js/console.log "Item updated.")]
        (map #(storage/save-budget-item % msg-fn) budget)
        updated)
      db))))

(re-frame/reg-event-db
 ::toggle-reset-item
 (fn-traced
  [db [_ _]]
  (update db :reset-item not)))

(re-frame/reg-event-db
 ::reset-item
 (fn-traced
  [db [_ item]]
  (let [updated (update db :budget budget/reset-item item)
        item (->> updated
                 :budget
                 (filter #(= (:budget-item-id item) (:budget-item-id %)))
                 first)]
    (if (s/valid? ::specs/budget (:budget updated))
      (do
        (storage/save-budget-item item #(js/console.log "Item updated."))
        (assoc updated :reset-item false))
      db))))

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

(re-frame/reg-event-db
 ::set-view-mode
 (fn-traced
  [db [_ view-mode]]
  (assoc db :view-mode view-mode)))

(re-frame/reg-event-db
 ::start-adjusting-income
 (fn-traced
  [db [_ amount]]
  (assoc db :income-adjustment {:amount amount})))

(re-frame/reg-event-db
 ::stop-adjusting-income
 (fn-traced
  [db [_ _]]
  (assoc db :income-adjustment nil)))

(re-frame/reg-event-db
 ::set-income
 (fn-traced
  [db [_ amount]]
  (assert ::specs/amount amount)
  (assoc-in db [:income-adjustment :amount] amount)))

(re-frame/reg-event-db
 ::set-income-error
 (fn-traced
  [db [_ msg]]
  (assert string? msg)
  (assoc-in db [:income-adjustment :error] msg)))

(re-frame/reg-event-db
 ::adjust-income
 (fn-traced
  [db _]
  (let [amount (-> db :income-adjustment :amount)
        updated (-> db
                    (assoc-in [:plan :income] amount)
                    (update :income-adjustment not))]
    (reagent/after-render
     #(do
        (scroll-to-id "income-input")
        (focus "income-input")))
    (storage/save-plan-income amount #(js/console.log "Updated income."))
    updated)))
