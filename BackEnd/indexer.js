const mongoose = require("mongoose");
const { double } = require("webidl-conversions");

const indexerSchema = new mongoose.Schema({
  Word: {
    type: String,
  },
  URLs: {
    type: [String],
  },
  NTF: {
    type: [],
  },
  DF: {
    type: Number,
  },
  IDF: {
    type: Number,
  },
});

const indexerModel = new mongoose.model("indexer", indexerSchema);

module.exports = indexerModel;