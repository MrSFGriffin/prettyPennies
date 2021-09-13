import 'package:flutter/material.dart';
import 'package:flutter/cupertino.dart';
import 'package:step_progress_indicator/step_progress_indicator.dart';

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
  final categories = ['Food', 'Petrol', 'Household', 'Kids'];
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
            children: [
              Container(
                constraints: BoxConstraints(
                    minHeight: MediaQuery.of(context).size.height -
                        (2 * verticalPadding)
                ),
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
                    Padding(
                        child: CupertinoButton(
                          child: const Text(
                              'Add Transaction',
                              style: TextStyle(color: CupertinoColors.white)),
                          onPressed: () => {},
                          color: CupertinoColors.systemPink,
                        ),
                        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 20)
                    ),
                    Container(
                      constraints: BoxConstraints(
                          maxHeight: MediaQuery.of(context).size.height - 275
                      ),
                      child: ListView.builder(
                        padding: const EdgeInsets.symmetric(horizontal: 20),
                        shrinkWrap: true,
                        itemBuilder: (context, index) {
                          var content = const Text('');
                          if (index == categories.length) {
                          } else {
                          }
                          return Container(
                              decoration: BoxDecoration(
                                  border: Border(
                                      left: BorderSide(
                                        color: colours[index],
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
                                                selectedColor: colours[index],
                                                unselectedColor: lightColours[index],
                                                roundedEdges: Radius.circular(10),
                                              ),
                                              Padding(
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
  // //
  // class MyApp extends StatelessWidget {
  //   const MyApp({Key? key}) : super(key: key);
  //
  //   // This widget is the root of your application.
  //   @override
  //   Widget build(BuildContext context) {
  //     return MaterialApp(
  //       title: 'Pretty Pennies',
  //       theme: ThemeData(
  //         primarySwatch: Colors.blue,
  //       ),
  //       home: const MyHomePage(title: 'MONEY'),
  //     );
  //   }
  // }
  //
  // class MyHomePage extends StatefulWidget {
  //   const MyHomePage({Key? key, required this.title}) : super(key: key);
  //   final String title;
  //
  //   @override
  //   State<MyHomePage> createState() => _MyHomePageState();
  // }
  //
  // class _MyHomePageState extends State<MyHomePage> {
  //   int _counter = 0;
  //
  //   void _incrementCounter() {
  //     setState(() {
  //       _counter++;
  //     });
  //   }
  //
  //   @override
  //   Widget build(BuildContext context) {
  //     return Scaffold(
  //       appBar: AppBar(
  //         centerTitle: true,
  //         title: Text(widget.title),
  //       ),
  //       body: Center(
  //         child: Column(
  //           mainAxisAlignment: MainAxisAlignment.center,
  //           children: <Widget>[
  //             const Text(
  //               'You have pushed the button this many times:',
  //             ),
  //             Text(
  //               '$_counter',
  //               style: Theme.of(context).textTheme.headline4,
  //             ),
  //           ],
  //         ),
  //       ),
  //       floatingActionButton: FloatingActionButton(
  //         onPressed: _incrementCounter,
  //         tooltip: 'Increment',
  //         child: const Icon(Icons.add),
  //       ),
  //     );
  //   }
  // }
