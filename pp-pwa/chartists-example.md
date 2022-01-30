

       [:> ui/Grid.Column
        [:div
         {:class "ct-chart"}
         [:h3 "Chart"]]]

    (reagent/after-render #(c/Pie ".ct-chart" #js{:series #js[#js{:value 10 :name "A"}
                                                             #js{:value 20 :name "B"}]}))
