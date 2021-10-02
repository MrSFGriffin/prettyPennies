import 'dart:collection';
import 'package:flutter/material.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'package:step_progress_indicator/step_progress_indicator.dart';
import 'package:sqflite/sqflite.dart';

void main() {
  runApp(
    ChangeNotifierProvider(
      create: (context) => BudgetModel(currencySymbol: euroSymbol),
      child: const CupApp()
    ));
}

class CupApp extends StatelessWidget {
  const CupApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return const CupertinoApp(
      home: Money(),
      title: 'Pretty Pennies',
      theme: CupertinoThemeData(
        primaryColor: CupertinoColors.systemBlue
      )
    );
  }
}

class Money extends StatefulWidget {
  const Money({Key? key}) : super(key : key);
  
  @override
  State<Money> createState() => _money();
}

const euroSymbol = '€';

class _money extends State<Money> {
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
    return ScreenContainer(
      child: BudgetScreen(
          colours: colours,
          lightColours: lightColours
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
  final Color colour;
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
        child: Padding(
          child: child,
          padding: const EdgeInsets.all(10),
        ),
        padding: const EdgeInsets.only(bottom: 0)
      )
    );
  }
}

class BudgetModel extends ChangeNotifier {
  BudgetModel({
    required this.currencySymbol,
  });

  final String currencySymbol;
  final LinkedHashMap<String, BudgetLineModel> _lines =
    LinkedHashMap<String, BudgetLineModel>();

  List<BudgetLineModel> lineItems() => _lines.values.toList();
  bool hasLineItems() => _lines.values.isNotEmpty;

  resetSpending() {
    for (var line in _lines.values) {
      line.resetSpending();
    }

    notifyListeners();
  }

  spend(String name, int amount) {
    if (!_lines.containsKey(name)) {
      return;
    }

    _lines[name]?.spend(amount);
    notifyListeners();
  }

  addLine(String name, int amount) {
    if (_lines.containsKey(name)) {
      // todo: maybe, add some kind of user message
      return;
    }

    _lines[name] = BudgetLineModel(
      name: name,
      amount: amount,
      currencySymbol: currencySymbol,
      spent: 0
    );
    notifyListeners();
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

  resetSpending() {
    _spent = 0;
  }

  spent() => _spent;

  left() => amount - _spent;

  keyInfoDisplay() => left() >= 0
      ? 'left $currencySymbol${left()}'
      : 'overspent $currencySymbol${-left()}';
}

class ColouredBudgetLine extends StatelessWidget {
  const ColouredBudgetLine(
      {required this.budgetLine,
       required this.fillColour,
       required this.emptyColour,
        Key? key})
       : super(key: key);

  final BudgetLineModel budgetLine;
  final MaterialColor fillColour;
  final Color emptyColour;

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
                  style: TextStyle(
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                      color: budgetLine.spent() > budgetLine.amount
                        ? CupertinoColors.destructiveRed
                        : CupertinoColors.black)
              )
            ]
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              StepProgressIndicator(
                totalSteps: budgetLine.amount,
                currentStep: budgetLine.spent() >= 0
                  ? budgetLine.spent() > budgetLine.amount
                    ? budgetLine.amount
                    : budgetLine.spent()
                  : 0,
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

class AddCategoryUI extends StatefulWidget {
  const AddCategoryUI({Key? key}) : super(key: key);

  @override
  State<AddCategoryUI> createState() => _AddCategory();
}

class _AddCategory extends State<AddCategoryUI> {
  var adding = false;
  var category = "";
  var amount = 0;

  void toggleAdding() {
    setState(() { adding = !adding; });
  }

  void addBudgetLine(BudgetModel budget) {
    budget.addLine(category, amount);
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<BudgetModel>(
      builder: (context, budget, child) => Row(children: [
          adding
          ? Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children:[
              Padding(
                child: Row(
                  children: [
                    Column(
                      children: const [
                        Padding(
                            child: Text("Category"),
                            padding: EdgeInsets.only(bottom: 10, left: 10, right: 10)),
                        Padding(
                            child: Text("Amount"),
                            padding: EdgeInsets.only(top: 10, left: 10, right: 10)),
                      ]
                    ),
                    Column(
                      children: [
                        Padding(
                          child: SizedBox(
                              child: CupertinoTextField(
                                onChanged: (c) => category = c,
                                placeholder: "thing to budget",
                              ),
                              width: 200),
                          padding: const EdgeInsets.only(bottom: 10, left: 10, right: 10)
                        ),
                        SizedBox(
                            child: NumericTextField(
                              onChanged: (a) => a == ''
                                ? amount = 0
                                : amount = int.parse(a),
                              placeholder: "max to spend"
                            ),
                            width: 200)
                      ]
                    ),
                  ]
                ),
                padding: const EdgeInsets.only(bottom: 10)
              ),
              Row(
                children: [
                  Padding(
                    child: Consumer<BudgetModel>(
                      builder: (context, budget, child) =>
                        PinkButton(
                          text: 'Add',
                          onPressed: () => addBudgetLine(budget)
                        ),
                    ),
                    padding: const EdgeInsets.all(10)
                  ),
                  Padding(
                      child: PinkButton(
                        text: 'Cancel',
                        onPressed: () => toggleAdding()
                      ),
                      padding: const EdgeInsets.all(10)
                  ),
                ]
              )
            ]
          )
          : PinkButton(
            text: 'Add Category',
            onPressed: () => toggleAdding()
          )
      ])
    );
  }
}

class ResetButton extends StatelessWidget{
  const ResetButton({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Consumer<BudgetModel>(
      builder: (context, budget, child) => PinkButton(
        text: 'Reset Spending',
        onPressed: () => budget.resetSpending(),
    ));
  }
}

class PinkButton extends StatelessWidget {
  const PinkButton({
    required this.text,
    required this.onPressed,
    Key? key
  }) : super(key : key);

  final String text;
  final VoidCallback? onPressed;

  @override
  Widget build(BuildContext context) {
    return
      CupertinoButton(
        child: Text(text, style: const TextStyle(color: CupertinoColors.white)),
        onPressed: onPressed,
        color: CupertinoColors.systemPink,
      );
  }
}
class SpendUI extends StatefulWidget {
  const SpendUI({Key? key}) : super(key: key);

  @override
  State<SpendUI> createState() => _SpendUI();
}

class _SpendUI extends State<SpendUI> {
  var _spending = false;
  var _category = "none selected";
  var _tempCategory = "";
  var _selectedIndex = 0;
  var _tempSelectedIndex = 0;
  var _amount = 0;

  toggleSpending() {
    setState(() { _spending = !_spending; });
  }

  pickCategory(context, budget) {
    showCupertinoDialog(
      context: context,
      builder: (BuildContext context) {
        return Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            SizedBox(
              child: CupertinoPicker(
                scrollController: FixedExtentScrollController(
                  initialItem: _selectedIndex,
                ),
                backgroundColor: Colors.white,
                onSelectedItemChanged: (index) {
                  setState(() {
                    _tempSelectedIndex = index;
                    _tempCategory = budget.lineItems()[index].name;
                  });
                },
                itemExtent: 32.0,
                children: budget.lineItems().map<Widget>((bl) => Text(bl.name)).toList()
              ),
              height: 200
            ),
            PinkButton(
              text: 'Pick',
              onPressed: () {
                setState(() {
                  _category = _tempCategory;
                  _selectedIndex = _tempSelectedIndex;
                });
                Navigator.of(context, rootNavigator: true).pop();
              }
            )
          ]
        );
      });
  }

  @override
  Widget build(BuildContext context) {
    return _spending
      ? Column(
          children: [
            Padding(
              child: Row(
                children: [
                  const Padding(
                    child:Text('Category'),
                    padding: EdgeInsets.only(right: 10),
                  ),
                  Padding(
                    child: Text(_category),
                    padding: const EdgeInsets.only(right: 10),
                  ),
                  Consumer<BudgetModel>(
                      builder: (context, budget, child) =>  PinkButton(
                          text: "Pick",
                          onPressed: () => pickCategory(context, budget)
                      )
                  ),
                ]
              ),
              padding: const EdgeInsets.only(bottom: 10),
            ),
            Padding(
              child: Row(
                  children: [
                    const Padding(
                      child: Text('Amount'),
                      padding: EdgeInsets.only(right: 10),
                    ),
                    SizedBox(
                        child: NumericTextField(
                          onChanged: (a) => setState(() { _amount = int.parse(a); }),
                          placeholder: 'how much spent',
                        ),
                        width: 200
                    ),
                  ]
              ),
              padding: const EdgeInsets.only(bottom: 10),
            ),
            Row(
              children: [
                Padding(
                  child: Consumer<BudgetModel>(
                    builder: (context, budget, child) => PinkButton(
                      text: 'Add',
                      onPressed: () {
                        budget.spend(_category, _amount);
                        toggleSpending();
                      }
                    )
                  ),
                  padding: const EdgeInsets.only(right: 10),
                ),
                PinkButton(
                    text: 'Cancel',
                    onPressed: () => toggleSpending()
                )
              ]
            )
          ]
        )
      : Consumer<BudgetModel>(
          builder: (context, budget, child) => PinkButton(
            text: 'Spend',
            onPressed: budget.hasLineItems()
              ? () => toggleSpending()
              : null
          )
        );
  }
}

class NumericTextField extends StatelessWidget {
  const NumericTextField({
    required this.onChanged,
    this.placeholder = "",
    Key? key
  }) : super(key : key);

  final ValueChanged<String>? onChanged;
  final String placeholder;

  @override
  Widget build(BuildContext context) {
    return CupertinoTextField(
      onChanged: onChanged,
      keyboardType: const TextInputType.numberWithOptions(),
      inputFormatters: <TextInputFormatter>[
        FilteringTextInputFormatter.allow(RegExp(r'[0-9]')),
      ],
      placeholder: placeholder,
    );
  }
}

class ScreenContainer extends StatelessWidget {
  const ScreenContainer({
    required this.child,
    Key? key
  }) : super(key : key);

  final verticalPadding = 60.0;
  final Widget child;

  @override
  Widget build(BuildContext context) {
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
                          child: child
                      )
                    ]
                ),
                minimum: EdgeInsets.symmetric(
                    horizontal: 20, vertical: verticalPadding),
              )
          )
      );

  }
}

class BudgetScreen extends StatelessWidget {
  const BudgetScreen({
    required this.colours,
    required this.lightColours,
    Key? key
  }) : super(key : key);

  final List<MaterialColor> colours;
  final List<Color> lightColours;

  @override
  Widget build(BuildContext context) {
    return
      Column(
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
          const Padding(
              child: SpendUI(),
              padding: EdgeInsets.symmetric(horizontal: 20, vertical: 20)
          ),
          Padding(
              child: Container(
                  constraints: BoxConstraints(
                      maxHeight: MediaQuery.of(context).size.height - 275
                  ),
                  child: Consumer<BudgetModel>(
                    builder: (context, budget, child) =>
                        ListView.builder(
                            padding: const EdgeInsets.symmetric(horizontal: 20),
                            shrinkWrap: true,
                            itemBuilder: (context, index) {
                              return ColouredListItem(
                                  colour: colours[index % colours.length],
                                  isLastItem: index >= budget.lineItems().length,
                                  child: index == budget.lineItems().length
                                      ? Row(children: const [AddCategoryUI()])
                                      : ColouredBudgetLine(
                                    budgetLine: budget.lineItems()[index],
                                    fillColour: colours[index % colours.length],
                                    emptyColour: lightColours[index % lightColours.length],
                                  )
                              );
                            },
                            itemCount: budget.lineItems().length + 1),
                  )
              ),
              padding: const EdgeInsets.only(bottom: 0)
          ),
          const Padding(
              child: ResetButton(),
              padding: EdgeInsets.symmetric(horizontal: 30, vertical: 20)
          )
        ],
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.start,
      );
  }
}