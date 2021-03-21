var Widget = function () {};
Widget.prototype.access = function() {};

var Client = {
  getFile: function(path, opts) {},
  storeFile: function(mime,path,data){},
  getListing: function(path,opts){}
};

var LWRemoteStorage = function (opts) {};
LWRemoteStorage.prototype.setApiKeys = function(opts) {};
LWRemoteStorage.prototype.scope = function(scope) { return Client; };
LWRemoteStorage.prototype.access = {claim: function(path, perms) {}};
LWRemoteStorage.prototype.disconnect = function() {};
LWRemoteStorage.prototype.on = function(evt, fn) {};

