const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

const create = require('./create');
const del = require('./delete');

exports.create = create.onCreateRequest;
exports.del = del.onDeleteRequest;

