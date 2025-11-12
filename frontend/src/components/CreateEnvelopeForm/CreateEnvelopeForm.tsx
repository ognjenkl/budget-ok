import { Button, Form, Input, InputNumber, message } from "antd";
import type { InputRef } from "antd";
import { useEffect, useRef } from "react";
import useCreateEnvelope from "../../hooks/useCreateEnvelope";

type CreateEnvelopeFormData = {
  name: string;
  budget: number;
};

type CreateEnvelopeFormProps = {
  onSuccess?: () => void;
};

export default function CreateEnvelopeForm({ onSuccess }: CreateEnvelopeFormProps) {
  const [form] = Form.useForm();
  const nameInputRef = useRef<InputRef>(null);
  const { mutate, isPending } = useCreateEnvelope();

  useEffect(() => {
    // Focus the name input when the component mounts
    nameInputRef.current?.focus();
  }, []);

  const onFinish = (values: CreateEnvelopeFormData) => {
    mutate(values, {
      onSuccess: () => {
        message.success('Envelope created successfully');
        form.resetFields();
        onSuccess?.();
      }
    });
  };

  return (
    <Form<CreateEnvelopeFormData>
      data-testid="create-envelope-form"
      layout="vertical"
      form={form}
      onFinish={onFinish}
    >
      <Form.Item 
        name="name" 
        label="Envelope Name" 
        rules={[{ required: true, message: 'Please input the envelope name' }]}
      >
        <Input 
          ref={nameInputRef} 
          placeholder="Envelope Name" 
          autoFocus 
        />
      </Form.Item>
      <Form.Item 
        name="budget" 
        label="Budget" 
        rules={[{ required: true, message: 'Please input the budget amount' }]}
      >
        <InputNumber 
          placeholder="Budget" 
          style={{ width: '100%' }} 
          min={0}
          step={0.01}
          precision={2}
        />
      </Form.Item>
      <Form.Item>
        <Button 
          type="primary" 
          htmlType="submit" 
          loading={isPending} 
          disabled={isPending}
        >
          Create Envelope
        </Button>
      </Form.Item>
    </Form>
  );
}