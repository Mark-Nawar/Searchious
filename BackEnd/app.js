const express = require("express");
const app = express();
const mongoose = require("mongoose");
const { getSystemErrorMap } = require("util");
const indexerModel = require("./indexer");
const indexer = require("./indexer");
app.use(express.static("public"));
// listen for requests
app.listen(3000, () => {
  console.log("server started on port 3000");
});

var stemmer = require("porter-stemmer").stemmer;
var url =
  "mongodb+srv://mohammedzaki:zaki@cluster0.qjawu.mongodb.net/?retryWrites=true&w=majority";

mongoose
  .connect(url)
  .then((res) => {
    console.log("Connected");
  })
  .catch((err) => {
    console.log(err);
  });

app.get("/search/:data", async (req, res) => {
  

  let arr = [];
  console.log(req.params.data);

  var search = stemmer(req.params.data);

  // MongoClient.connect(url, async function (err, db) {
  //     if (err) throw err;
  //     var  tmp = await db.collection("Indexer").findOne({ Word: search })
  //     console.log(tmp);
  // });
  const result = await indexer.findOne({Word : search});

  await newModel.save();
  
  res.status(200).send(result);
  console.log(result);
});
