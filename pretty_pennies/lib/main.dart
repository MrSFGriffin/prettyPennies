import 'package:flutter/material.dart';
import 'package:flutter/cupertino.dart';
import 'package:step_progress_indicator/step_progress_indicator.dart';
import 'package:sqflite/sqflite.dart';

void main() {
  //runApp(const MyApp());
  runApp(const CupApp());
}

class CupApp extends StatelessWidget {
  const CupApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return CupertinoApp(
      home: const Money(),
      title: 'Pretty Pennies',
      theme: CupertinoThemeData(
        primaryColor: CupertinoColors.systemBlue,
      )
    );
  }
}

class Money extends StatefulWidget {
  const Money({Key? key}) : super(key : key);

  @override
  State<Money> createState() => _money();
}

class _money extends State<Money> {
  final verticalPadding = 60.0;
  final categories = [
    'Food', 'Petrol', 'Household', 'Kids',
  ];
  final colours = [
    Colors.yellow, Colors.green,
    Colors.pink, Colors.blue,
    Colors.orange
  ];
  final lightColours = [
    Colors.yellow.shade200, Colors.green.shade200,
    Colors.pink.shade200, Colors.blue.shade200,
    Colors.orange.shade200
  ];
  @override
  build(BuildContext context) {
    return Container(
        decoration: const BoxDecoration (
          image: DecorationImage(
            fit: BoxFit.fill,
            image: AssetImage("assets/images/flower.jpg"),
          ),
        ),
        child: CupertinoPageScaffold(
            backgroundColor: Colors.transparent,
        //navigationBar: const CupertinoNavigationBar(middle: Text('MONEY')),
        child: SafeArea(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Container(
                // constraints: BoxConstraints(
                //   maxHeight: MediaQuery.of(context).size.height -
                //              (2 * verticalPadding)
                // ),
                decoration: const BoxDecoration(
                  borderRadius: BorderRadius.all(Radius.circular(20)),
                  color: CupertinoColors.systemBackground,
                ),
                child: Column(
                  children: [
                    Padding(
                      child: Row(
                        children: const [
                          Text(
                            'MONEY',
                            style: TextStyle(
                                fontSize: 40,
                                fontWeight: FontWeight.bold,
                                fontFamily: "Helvetica"),
                          )
                        ],
                        mainAxisAlignment: MainAxisAlignment.center,
                      ),
                      padding: const EdgeInsets.only(top: 20)
                    ),
                    Row(
                      children: [

                      ]
                    ),
                    Padding(
                        child: CupertinoButton(
                          child: const Text(
                              'Spend',
                              style: TextStyle(color: CupertinoColors.white)),
                          onPressed: () => {},
                          color: CupertinoColors.systemPink,
                        ),
                        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 20)
                    ),
                    Padding(
                      child: Container(
                        constraints: BoxConstraints(
                            maxHeight: MediaQuery.of(context).size.height - 275
                        ),
                        child: ListView.builder(
                          padding: const EdgeInsets.symmetric(horizontal: 20),
                          shrinkWrap: true,
                          itemBuilder: (context, index) {
                            return
                              Container(
                                decoration: BoxDecoration(
                                    border: Border(
                                        left: BorderSide(
                                          color: colours[index % colours.length],
                                          width: 4,
                                        ),
                                        top: const BorderSide(
                                            color: CupertinoColors.systemTeal,
                                            width: 1
                                        ),
                                        bottom: BorderSide(
                                            color: CupertinoColors.systemTeal,
                                            width: index == categories.length
                                                ? 1 : 0
                                        )
                                    )
                                ),
                                child: Padding(
                                  child: index == categories.length
                                      ? Row(
                                        children: [
                                          CupertinoButton(
                                            child: const Text(
                                                'Add Category',
                                                style: TextStyle(
                                                    color: CupertinoColors.white)),
                                            onPressed: () => {},
                                            color: CupertinoColors.systemPink,
                                          )
                                        ]
                                      )
                                      : Column(
                                          children: [
                                            Row(
                                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                                children: [
                                                  Text(
                                                    categories[index],
                                                    style: const TextStyle(
                                                      fontSize: 20
                                                    )
                                                  ),
                                                  const Text(
                                                      'left €200',
                                                      style: TextStyle(
                                                          fontSize: 24,
                                                          fontWeight: FontWeight.bold
                                                      )
                                                  )
                                                ]
                                            ),
                                            Row(
                                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                              children: [
                                                StepProgressIndicator(
                                                  totalSteps: 100,
                                                  currentStep: 32,
                                                  //customStep: (i, c, d) => Text('|'),
                                                  size: 8,
                                                  padding: 0,
                                                  selectedColor: colours[index % colours.length],
                                                  unselectedColor: lightColours[index % colours.length],
                                                  roundedEdges: Radius.circular(10),
                                                ),
                                                const Padding(
                                                  child: Text('€400'),
                                                  padding: EdgeInsets.only(top: 5)
                                                )
                                              ]
                                            )
                                          ]
                                      ),
                                padding: const EdgeInsets.all(10),
                              )
                            );
                          },
                          itemCount: categories.length + 1),
                      ),
                      padding: const EdgeInsets.only(bottom: 0)
                    ),
                    Padding(
                      child: CupertinoButton(
                        child: const Text(
                            'Reset Spending',
                            style: TextStyle(color: CupertinoColors.white)),
                        onPressed: () => {},
                        color: CupertinoColors.systemPink,
                      ),
                      padding: const EdgeInsets.symmetric(horizontal: 30, vertical: 20)
                    )
                  ],
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisAlignment: MainAxisAlignment.start,
                ),
              ),
            ]
          ),
          minimum: EdgeInsets.symmetric(
              horizontal: 20, vertical: verticalPadding),
        )
      )
    );
  }
}

class ColouredListItem extends StatelessWidget {
  const ColouredListItem({
      required this.child,
      required this.colour,
      required this.isLastItem,
      Key? key}) : super(key: key);

  final Widget child;
  final MaterialColor colour;
  final bool isLastItem;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
          border: Border(
              left: BorderSide(
                color: colour,
                width: 4,
              ),
              top: const BorderSide(
                  color: CupertinoColors.systemTeal,
                  width: 1
              ),
              bottom: BorderSide(
                  color: CupertinoColors.systemTeal,
                  width: isLastItem ? 1 : 0
              )
          ),
      ),
      child: Padding(
        child: child,
        padding: const EdgeInsets.only(bottom: 0)
      )
    );
  }
}

// make BudgetLineModel a stateful widget.
class BudgetLineModel {
  BudgetLineModel({
    required this.name,
    required this.currencySymbol,
    required this.amount,
    required spent
  }) : _spent = spent;

  final String name;
  final String currencySymbol;
  final int amount;
  int _spent;

  spend(int amount) {
    _spent += amount;
  }

  spent() => _spent;

  left() => amount - _spent;

  keyInfoDisplay() => left() >= 0
      ? "left $currencySymbol${left()}"
      : "overspent $currencySymbol${-left()}";
}

class ColouredBudgetLine extends StatelessWidget {
  const ColouredBudgetLine(
      {required this.budgetLine,
       required this.fillColour,
       required this.emptyColour, Key? key})
       : super(key: key);

  final BudgetLineModel budgetLine;
  final MaterialColor fillColour;
  final MaterialColor emptyColour;

  @override
  Widget build(BuildContext context) {
    return Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                budgetLine.name,
                style: const TextStyle(fontSize: 20)
              ),
              Text(
                  budgetLine.keyInfoDisplay(),
                  style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold)
              )
            ]
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              StepProgressIndicator(
                totalSteps: budgetLine.amount,
                currentStep: budgetLine.spent(),
                size: 8,
                padding: 0,
                selectedColor: fillColour,
                unselectedColor: emptyColour,
                roundedEdges: const Radius.circular(10),
              ),
              Padding(
                  child: Text('€${budgetLine.amount}'),
                  padding: const EdgeInsets.only(top: 5)
              )
            ]
          )
        ]
    );
  }
}

class AddCategoryButton extends StatelessWidget {
  const AddCategoryButton({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Row(children: [
      CupertinoButton(
        child: const Text(
            'Add Category',
            style: TextStyle(color: CupertinoColors.white)),
        onPressed: () => {},
        color: CupertinoColors.systemPink,
      )
    ]);
  }
}