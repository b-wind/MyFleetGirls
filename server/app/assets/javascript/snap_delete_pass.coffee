$(document).ready ->
  userId = parseInt($('#userid').val())
  snapId = parseInt($('#snapid').val())
  vue = new Vue
    el: '#delete_snap'
    data:
      errorMsg: ""
    methods:
      submit: () ->
        data = {userId: userId, snapId: snapId}
        $.post('/passwd/post/v1/delete_snap', data)
        .done( -> location.href = "/user/#{userId}/snapshot")
        .fail((str) => @errorMsg = str)
