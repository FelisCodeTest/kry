const listContainer = document.querySelector('#service-list');
let servicesRequest = new Request('/service');
fetch(servicesRequest)
.then(function(response) { return response.json(); })
.then(function(serviceList) {
  serviceList.forEach(service => {
    var li = document.createElement("li");
    li.appendChild(document.createTextNode(service.name + ' - ' + service.url  + " => "+ service.status));
    li.appendChild(document.createTextNode(' - Created on : '  + service.date));
    var btn = document.createElement("BUTTON");
    btn.appendChild(document.createTextNode('Delete'));
    btn.onclick = evt => {
           let name = service.name;
           fetch('/service', {
           method: 'delete',
           headers: {
           'Accept': 'application/json, text/plain, */*',
           'Content-Type': 'application/json'
           },
         body: JSON.stringify({name:name})
       }).then(res=> location.reload());
       };

    li.appendChild(btn);
    listContainer.appendChild(li);
  });
});

const saveButton = document.querySelector('#post-service');
saveButton.onclick = evt => {
    let newName = document.querySelector('#name').value;
    let newUrl = document.querySelector('#url').value;
    fetch('/service', {
    method: 'post',
    headers: {
    'Accept': 'application/json, text/plain, */*',
    'Content-Type': 'application/json'
    },
  body: JSON.stringify({url:newUrl, name:newName})
}).then(res=> location.reload());
}