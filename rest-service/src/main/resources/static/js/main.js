function validate_Account_Edit_Form(){

    var email_pattern = new RegExp("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
    var password_pattern = new RegExp("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@!#$%^&+=])(?=\\S+$).{8,}$");

    var edit_email_input = $('#email_edit');
    var edit_password_input = $('#password_edit');

    if(email_pattern.test(edit_email_input.val())){
    /*email is correct*/
     $('#edit_email_input_group').removeClass("form-group has-error has-feedback");
     $('#edit_email_alert').hide();

        if(password_pattern.test(edit_password_input.val())){
            /*password correct*/
            $('#edit_password_alert').hide();
            $('#edit_password_input_group').removeClass("form-group has-error has-feedback");
            $('#editForm').submit();
        }else{
            $('#edit_password_input_group').addClass("form-group has-error has-feedback");
            $('#edit_password_alert').show();
         }
     }else{
        $('#edit_email_input_group').addClass("form-group has-error has-feedback");
        $('#edit_email_alert').show();
        }
}

function resetModal(){

    var edit_email_input = $('#email_edit');
    var edit_password_input = $('#password_edit');

    edit_email_input.val($('#email').html());
    edit_password_input.val($('#password').val());

    $('#edit_email_alert').hide();
    $('#edit_email_input_group').removeClass("form-group has-error has-feedback");

    $('#edit_password_alert').hide();
    $('#edit_password_input_group').removeClass("form-group has-error has-feedback");
}

function validate_Change_Password_Form(){
    var password_pattern = new RegExp("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@!#$%^&+=])(?=\\S+$).{8,}$");
    var password_input = $('#password');
    if(password_pattern.test(password_input.val())){
        /*password is correct*/
        $('#password_input_group').removeClass("form-group has-error has-feedback");
        $('#edit_password_alert').hide();
        $('#password_form').submit();
    }else{
        $('#password_input_group').addClass("form-group has-error has-feedback");
        $('#edit_password_alert').show();
    }
}


function generateNewTranslation(){
    $('#generate_new_translation_button').html('<span class="glyphicon glyphicon-refresh glyphicon-refresh-animate"></span> Loading...');
    var id = [[${id}]];
    var key = [[${key}]];
     var iv =  [[${iv}]];
    var newToLang = $("#target_language_selector option:selected").text().split(" -")[0].split(" ")[0];
    var newProvider = $('#translation_provider').val();
    var url = 'https://localhost:8443/newtranslation/?id=' +id+ '&key=' +key+ '&iv=' +iv+ '&newToLang=' +newToLang+ '&newProvider=' +newProvider;

    $.ajax({
        type: 'get',
        url: url,
        success: function (response) {
            $('#target_text').val(response);	// set new target content
            $('#generate_new_translation_button').html('Generate translation');
            },
            error: function (xhr, ajaxOptions, thrownError) {
            if(xhr.status==400){
                 alert('HTPP error: '+ xhr.status + ' Please ensure, you have different source and target languages. If you do so, then mymemory does not support the target language. Please try another provider.');
             }
            $('#generate_new_translation_button').html('Generate translation');
        }
    });
}